package me.nethuli.ticketingsystem.controller;

import lombok.extern.slf4j.Slf4j;
import me.nethuli.ticketingsystem.config.TicketingDefaultProperties;
import me.nethuli.ticketingsystem.dto.*;
import me.nethuli.ticketingsystem.helper.TicketConfiguration;
import me.nethuli.ticketingsystem.service.TicketingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

// This class is the controller class for the ticketing system. It is responsible for handling all the incoming requests and delegating the work to the service layer.
@RestController
@RequestMapping("/api/tickets")
@Slf4j
public class TicketSystemController {
    private final TicketingService ticketingService;

    @Autowired
    public TicketSystemController(TicketingService ticketingService, TicketingDefaultProperties defaultProperties) {
        this.ticketingService = ticketingService;
    }

    // This method is used to check the health of the system.
    @GetMapping("/health")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> get() {
        return ResponseEntity.ok("OK");
    }

    // This method is used to configure the ticketing system.
    @PatchMapping("/configure")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<TicketConfiguration> updateConfig(@RequestBody TicketConfigurationRequest config) {
        return ResponseEntity.ok(ticketingService.configureSystem(config));
    }

    // This method is used to get the current configuration of the ticketing system.
    @GetMapping("/configure")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<TicketConfiguration> getConfig() {
        return ResponseEntity.ok(ticketingService.getCurrentConfig());
    }

    // This method is used to start the ticketing operations.
    @PostMapping("/start")
    public ResponseEntity<Map<String, String>> startOperations() {
        ticketingService.startTicketOperations();
        return ResponseEntity.ok(Map.of("message", "Ticket operations started successfully"));
    }

    // This method is used to pause the ticketing operations.
    @PostMapping("/pause")
    public ResponseEntity<Map<String, String>> pauseOperations() {
        ticketingService.pauseTicketOperations();
        return ResponseEntity.ok(Map.of("message", "Ticket operations paused successfully"));
    }

    // This method is used to resume the ticketing operations.
    @PostMapping("/resume")
    public ResponseEntity<Map<String, String>> resumeOperations() {
        ticketingService.resumeTicketOperations();
        return ResponseEntity.ok(Map.of("message", "Ticket operations resumed successfully"));
    }

    // This method is used to stop the ticketing operations.
    @PostMapping("/stop")
    public ResponseEntity<Map<String, String>> stopOperations() {
        ticketingService.stopTicketOperations();
        return ResponseEntity.ok(Map.of("message", "Ticket operations stopped successfully"));
    }

    // This method is used to add a customer to the system.
    @PostMapping("/customer")
    public ResponseEntity<Map<String, String>> addCustomer(@RequestBody CustomerRequest request) {
        ticketingService.addCustomer(request);
        return ResponseEntity.ok(Map.of("message", "Customer " + request.getName() + " added successfully"));
    }

    // This method is used to add a vendor to the system.
    @PostMapping("/vendor")
    public ResponseEntity<Map<String, String>> addVendor(@RequestBody VendorRequest request) {
        ticketingService.addVendor(request, null);
        return ResponseEntity.ok(Map.of("message", "Vendor " + request.getName() + " added successfully"));
    }

    // This method is used to get the status of the system.
    @GetMapping("/sot")
    public ResponseEntity<Map<String, List<SalesOverTimeResponse>>> getSalesOverTime() {
        List<SalesOverTimeResponse> response = ticketingService.getSalesOverTime(LocalDateTime.now());
        return ResponseEntity.ok(Map.of("sot", response));
    }

    // This method is used to get the status of the system.
    @GetMapping("/status")
    public ResponseEntity<SystemStatusResponse> getSystemStatus() {
        SystemStatusResponse status = ticketingService.getSystemStatus();
        return ResponseEntity.ok(status);
    }
}
