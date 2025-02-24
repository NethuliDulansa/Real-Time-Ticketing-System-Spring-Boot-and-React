package me.nethuli.ticketingsystem.service.impl;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import me.nethuli.ticketingsystem.config.TicketingDefaultProperties;
import me.nethuli.ticketingsystem.dto.*;
import me.nethuli.ticketingsystem.helper.LoggingHelper;
import me.nethuli.ticketingsystem.helper.TicketConfiguration;
import me.nethuli.ticketingsystem.helper.TicketPool;
import me.nethuli.ticketingsystem.model.Customer;
import me.nethuli.ticketingsystem.model.Vendor;
import me.nethuli.ticketingsystem.repository.CustomerRepository;
import me.nethuli.ticketingsystem.repository.TicketRepository;
import me.nethuli.ticketingsystem.service.TicketingService;
import me.nethuli.ticketingsystem.service.WebSocketMessageService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketingServiceImpl implements TicketingService {
    private final TicketingDefaultProperties defaultProperties;
    private final WebSocketMessageService webSocketMessageService;
    private final TicketRepository ticketRepository;
    private final CustomerRepository customerRepository;

    private TicketPool ticketPool;
    private TicketConfiguration config;
    private boolean isRunning = false;
    private final Map<String, Thread> vendorThreads = new ConcurrentHashMap<>();
    private final Map<String, Thread> customerThreads = new ConcurrentHashMap<>();
    private volatile boolean isPaused = false;

    @PostConstruct
    public void init() {
        // Initialize with default configuration
        this.config = new TicketConfiguration(
                defaultProperties.getTotalTickets(),
                defaultProperties.getTicketReleaseRate(),
                defaultProperties.getCustomerRetrievalRate(),
                defaultProperties.getMaxTicketCapacity()
        );
    }

    @Override
    public TicketConfiguration configureSystem(TicketConfigurationRequest request) {
        if (isRunning) {
            String message = "Cannot update configuration while running";
            LoggingHelper.error(message);
            throw new IllegalStateException(message);
        }
        this.config = new TicketConfiguration(
                request.getTotalTickets(),
                request.getTicketReleaseRate(),
                request.getCustomerRetrievalRate(),
                request.getMaxTicketCapacity()
        );
        return this.config;
    }

    @Override
    public TicketConfiguration getCurrentConfig() {
        return this.config;
    }

    @Override
    public List<SalesOverTimeResponse> getSalesOverTime(LocalDateTime dateTime) {
        LocalDateTime startDateTime = dateTime.minusMinutes(1);
        List<Map<String, Object>> salesData = ticketRepository.findTicketsSoldPerSecond(startDateTime, dateTime);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        return salesData.stream()
                .map(data -> new SalesOverTimeResponse(
                        ((String) data.get("timestamp")).substring(11),
                        ((Number) data.get("count")).intValue()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public void startTicketOperations() {
        if (isRunning) {
            LoggingHelper.error("Ticket operations are already running.");
            throw new IllegalStateException("Ticket operations are already running.");
        }

        ticketPool = new TicketPool(config.getMaxTicketCapacity(), config.getTotalTickets(), webSocketMessageService, ticketRepository);
        vendorThreads.clear();
        customerThreads.clear();

        LoggingHelper.info("Ticket operations started");
        webSocketMessageService.sendLogMessage("Ticket operations started");

        // Create initial vendor with default configuration
        String initialVendorName = "Default-Vendor";
        addVendor(new VendorRequest(initialVendorName, defaultProperties.getTicketReleaseRate()), true);

        isRunning = true;
    }

    @Override
    public void addVendor(VendorRequest request, Boolean isInitial) {
        if ((isInitial != null && !isInitial) && !isRunning) {
            webSocketMessageService.sendLogMessage("Please start the system first.");
            LoggingHelper.error("Please start the system first.");
            throw new IllegalStateException("Please start the system first.");
        }
        if (vendorThreads.containsKey(request.getName())) {
            webSocketMessageService.sendLogMessage("Vendor already exists!");
            LoggingHelper.error("Vendor already exists!");
            throw new IllegalStateException("Vendor already exists!");
        }

        Vendor vendor = new Vendor(ticketPool, request.getReleaseRate(), request.getName(), webSocketMessageService);
        Thread vendorThread = new Thread(vendor);
        vendorThread.start();
        vendorThreads.put(request.getName(), vendorThread);
    }

    @Override
    public void addCustomer(CustomerRequest request) {
        if (!isRunning) {
            webSocketMessageService.sendLogMessage("Please start the system first.");
            LoggingHelper.error("Please start the system first.");
            throw new IllegalStateException("Please start the system first.");
        }
        if (customerThreads.containsKey(request.getName())) {
            LoggingHelper.error("Customer already exists!");
            throw new IllegalStateException("Customer already exists!");
        }

        Customer customer = new Customer(
                request.getName(),
                request.isVip(),
                request.getNumberOfTickets(),
                ticketPool,
                request.getRetrievalRate(),
                webSocketMessageService
        );
        customerRepository.save(customer);
        Thread customerThread = new Thread(customer);
        customerThread.setPriority(customer.isVip() ? Thread.MAX_PRIORITY : Thread.NORM_PRIORITY);
        customerThread.start();
        customerThreads.put(request.getName(), customerThread);
        String message = String.format("Customer %s%s added with retrieval rate as %d", customer.getName(), customer.isVip() ? "(VIP)" : "", customer.getRetrievalRate());
        webSocketMessageService.sendLogMessage(message);
    }

    @Override
    public void stopTicketOperations() {
        if (!isRunning) {
            webSocketMessageService.sendLogMessage("Ticket operations are not running.");
            LoggingHelper.error("Ticket operations are not running.");
            throw new IllegalStateException("Ticket operations are not running.");
        }

        if (ticketPool != null) {
            ticketPool.stopSystem();
        }

        // Stop all vendors
        vendorThreads.forEach((name, thread) -> {
            thread.interrupt();
            try {
                thread.join(1000);
            } catch (InterruptedException e) {
                webSocketMessageService.sendLogMessage("Interrupted while waiting for vendor " + name + " to stop");
                LoggingHelper.warn("Interrupted while waiting for vendor " + name + " to stop");
            }
        });

        // Stop all customers
        customerThreads.forEach((name, thread) -> {
            thread.interrupt();
            try {
                thread.join(1000);
            } catch (InterruptedException e) {
                webSocketMessageService.sendLogMessage("Interrupted while waiting for customer " + name + " to stop");
                LoggingHelper.warn("Interrupted while waiting for customer " + name + " to stop");
            }
        });

        // Clean up
        vendorThreads.clear();
        customerThreads.clear();
        ticketPool = null;
        isRunning = false;
        webSocketMessageService.sendLogMessage("Ticket Operation Stopped.");
        LoggingHelper.info("Ticket Operation Stopped.");

    }


    @Override
    public void pauseTicketOperations() {
        if (!isRunning) {
            webSocketMessageService.sendLogMessage("Ticket operations are not running.");
            LoggingHelper.error("Ticket operations are not running.");
            throw new IllegalStateException("Ticket operations are not running.");
        }
        if (isPaused) {
            webSocketMessageService.sendLogMessage("Ticket operations are already paused.");
            LoggingHelper.error("Ticket operations are already paused.");
            throw new IllegalStateException("Ticket operations are already paused.");
        }

        webSocketMessageService.sendLogMessage("Pausing ticket operations...");
        LoggingHelper.info("Pausing ticket operations...");
        isPaused = true;
        ticketPool.setPaused(true);
    }

    @Override
    public void resumeTicketOperations() {
        if (!isRunning) {
            webSocketMessageService.sendLogMessage("Ticket operations are not running.");
            LoggingHelper.error("Ticket operations are not running.");
            throw new IllegalStateException("Ticket operations are not running.");
        }
        if (!isPaused) {
            webSocketMessageService.sendLogMessage("Ticket operations are already paused.");
            LoggingHelper.error("Ticket operations are already paused.");
            throw new IllegalStateException("Ticket operations are already paused.");
        }

        webSocketMessageService.sendLogMessage("Resuming ticket operations...");
        LoggingHelper.info("Resuming ticket operations...");
        isPaused = false;
        ticketPool.setPaused(false);
        ticketPool.notifyAllWaiting();
    }

    @Override
    public SystemStatusResponse getSystemStatus() {
        return new SystemStatusResponse(
                isRunning,
                isPaused,
                ticketPool != null ? ticketPool.getTicketCount() : 0,
                ticketPool != null ? ticketPool.getRemainingTotalTickets() : 0,
                vendorThreads.size(),
                customerThreads.size()
        );
    }
}
