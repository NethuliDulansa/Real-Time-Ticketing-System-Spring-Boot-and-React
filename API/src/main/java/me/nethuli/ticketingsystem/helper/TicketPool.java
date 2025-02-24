package me.nethuli.ticketingsystem.helper;

import me.nethuli.ticketingsystem.model.Customer;
import me.nethuli.ticketingsystem.model.Ticket;
import me.nethuli.ticketingsystem.repository.TicketRepository;
import me.nethuli.ticketingsystem.service.WebSocketMessageService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

/*
 * TicketPool class is responsible for managing the tickets in the system.
 */
public class TicketPool {
    private final ConcurrentLinkedQueue<Ticket> tickets;
    private final ReentrantLock lock;
    private final PriorityBlockingQueue<Customer> waitingCustomers;
    private final int maxCapacity;
    private int remainingTotalTickets;
    private volatile boolean isPaused = false;
    private final Object pauseLock = new Object();
    private final WebSocketMessageService webSocketMessageService;
    private final TicketRepository ticketRepository;
    private volatile boolean isSystemStopped = false;

    public TicketPool(int maxCapacity, int totalTickets, WebSocketMessageService webSocketMessageService,
            TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
        this.tickets = new ConcurrentLinkedQueue<>();
        this.lock = new ReentrantLock(true); // Fair locking
        this.waitingCustomers = new PriorityBlockingQueue<>();
        this.maxCapacity = maxCapacity;
        this.remainingTotalTickets = totalTickets;
        this.webSocketMessageService = webSocketMessageService;
    }

    /*
     * Pause the ticket pool.
     */
    public void setPaused(boolean paused) {
        this.isPaused = paused;
    }

    /*
     * Notify all waiting threads that the ticket pool is no longer paused.
     */
    public void notifyAllWaiting() {
        synchronized (pauseLock) {
            pauseLock.notifyAll();
        }
    }

    /*
     * Stop the ticket pool.
     */
    public void stopSystem() {
        isSystemStopped = true;
        notifyAllWaiting();
    }

    /*
     * Check if the ticket pool is stopped.
     */
    public boolean isSystemStopped() {
        return isSystemStopped;
    }

    /*
     * Check if the ticket pool is at maximum capacity.
     */
    public boolean isAtCapacity() {
        return tickets.size() >= maxCapacity;
    }

    /*
     * Add tickets to the ticket pool.
     */
    public void addTickets(List<Ticket> newTickets) {
        // Lock the pool to prevent multiple threads from adding tickets at the same
        // time
        lock.lock();
        try {
            while (isPaused) {
                // If the pool is paused, wait until it is unpaused
                lock.unlock();
                synchronized (pauseLock) {
                    pauseLock.wait();
                }
                lock.lock();
            }

            // Check if there are any tickets left to add
            if (remainingTotalTickets <= 0) {
                String message = "No more tickets available to add. Total tickets exhausted.";
                webSocketMessageService.sendLogMessage(message);
                LoggingHelper.info(message);
                return;
            }

            // Check if the pool is at maximum capacity
            if (isAtCapacity()) {
                String message = "Cannot add tickets. Pool is at maximum capacity.";
                webSocketMessageService.sendLogMessage(message);
                LoggingHelper.info(message);
                return;
            }

            // Add tickets to the pool up to the maximum capacity
            int ticketsToAdd = Math.min(
                    Math.min(maxCapacity - tickets.size(), newTickets.size()),
                    remainingTotalTickets);

            // Check if the pool would exceed the maximum capacity
            if (ticketsToAdd <= 0) {
                String message = "Cannot add tickets. Capacity would be exceeded.";
                webSocketMessageService.sendLogMessage(message);
                LoggingHelper.info(message);
                return;
            }

            // Add the tickets to the pool
            for (int i = 0; i < ticketsToAdd; i++) {
                Ticket ticket = newTickets.get(i);
                // Save the ticket to the database
                ticketRepository.save(ticket);
                // Add the ticket to the pool
                tickets.offer(ticket);
            }
            // Update the remaining total tickets
            remainingTotalTickets -= ticketsToAdd;
            processWaitingCustomers();

            String message = ticketsToAdd + " tickets added. Total tickets: " + tickets.size() +
                    ". Remaining total tickets: " + remainingTotalTickets;
            webSocketMessageService.sendLogMessage(message);
            LoggingHelper.info(message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

    /*
     * Process the waiting customers in the queue.
     */
    private void processWaitingCustomers() {
        // Serve customers in the waiting queue
        while (!tickets.isEmpty() && !waitingCustomers.isEmpty()) {
            // Get all customers from the queue
            Customer[] customers = waitingCustomers.toArray(new Customer[0]);
            waitingCustomers.clear();

            // First serve VIP customers
            for (Customer customer : customers) {
                if (customer.isVip() && !tickets.isEmpty()) {
                    // Poll is used to remove the head of the queue
                    Ticket ticket = tickets.poll();
                    if (ticket != null) {
                        ticket.setStatus(Ticket.TicketStatus.SOLD);
                        ticketRepository.save(ticket);
                        String message = customer.getName() + "(VIP) received reserved ticket: "
                                + ticket.getTicketName();
                        webSocketMessageService.sendLogMessage(message);
                        LoggingHelper.info(message);
                    }
                } else {
                    waitingCustomers.offer(customer);
                }
            }

            // Then serve remaining customers
            while (!tickets.isEmpty() && !waitingCustomers.isEmpty()) {
                Customer customer = waitingCustomers.poll();
                if (customer != null) {
                    Ticket ticket = tickets.poll();
                    if (ticket != null) {
                        ticket.setStatus(Ticket.TicketStatus.SOLD);
                        ticket.setSoldAt(LocalDateTime.now());
                        ticketRepository.save(ticket);
                        String message = customer.getName() + " received reserved ticket: " + ticket.getTicketName();
                        webSocketMessageService.sendLogMessage(message);
                        LoggingHelper.info(message);
                    }
                }
            }
        }
    }

    /*
     * Remove a ticket from the ticket pool.
     */
    public Ticket removeTicket(Customer customer) throws InterruptedException {
        if (isSystemStopped) {
            return null;
        }

        lock.lock();
        try {
            while (isPaused) {
                // If the pool is paused, wait until it is unpaused
                lock.unlock();
                synchronized (pauseLock) {
                    pauseLock.wait();
                }
                lock.lock();
            }

            if (isSystemStopped) {
                return null;
            }

            // First check if there are no more tickets available at all
            if (tickets.isEmpty() && remainingTotalTickets <= 0) {
                if (!isSystemStopped) {
                    String message = "All tickets have been sold. System will stop.";
                    webSocketMessageService.sendLogMessage(message);
                    LoggingHelper.info(message);
                    stopSystem();
                }
                return null;
            }

            // If no tickets available, add to waiting queue
            if (tickets.isEmpty()) {
                waitingCustomers.offer(customer);
                return null;
            }

            // Check if there are any VIP customers waiting
            boolean vipWaiting = false;
            for (Customer waitingCustomer : waitingCustomers) {
                if (waitingCustomer.isVip()) {
                    vipWaiting = true;
                    break;
                }
            }

            // If customer is not VIP and there are VIP customers waiting, add to queue
            if (!customer.isVip() && (vipWaiting || !waitingCustomers.isEmpty())) {
                waitingCustomers.offer(customer);
                return null;
            }

            // If customer is VIP, they get priority
            if (customer.isVip()) {
                Ticket ticket = tickets.poll();
                if (ticket != null) {
                    ticket.setStatus(Ticket.TicketStatus.SOLD);
                    ticket.setSoldAt(LocalDateTime.now());
                    ticketRepository.save(ticket);
                }
                return ticket;
            }

            // For non-VIP customers when no VIPs are waiting
            if (waitingCustomers.isEmpty()) {
                Ticket ticket = tickets.poll();
                if (ticket != null) {
                    ticket.setStatus(Ticket.TicketStatus.SOLD);
                    ticket.setSoldAt(LocalDateTime.now());
                    ticketRepository.save(ticket);
                }
                return ticket;
            }

            // Add to waiting queue if none of the above conditions are met
            waitingCustomers.offer(customer);
            return null;
        } finally {
            lock.unlock();
        }
    }

    /*
     * Get the number of tickets in the ticket pool.
     */
    public synchronized int getTicketCount() {
        return tickets.size();
    }

    /*
     * Get the number of remaining total tickets.
     */
    public synchronized int getRemainingTotalTickets() {
        return remainingTotalTickets;
    }

    /*
     * Check if there are any available tickets in the ticket pool.
     */
    public synchronized boolean hasAvailableTickets() {
        return !isSystemStopped && (remainingTotalTickets > 0 || !tickets.isEmpty());
    }

    /*
     * Check if the ticket pool is paused.
     */
    public boolean isPaused() {
        return isPaused;
    }
}
