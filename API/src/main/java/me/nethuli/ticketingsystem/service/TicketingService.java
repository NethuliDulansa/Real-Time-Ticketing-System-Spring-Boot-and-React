package me.nethuli.ticketingsystem.service;

import me.nethuli.ticketingsystem.dto.*;
import me.nethuli.ticketingsystem.helper.TicketConfiguration;

import java.time.LocalDateTime;
import java.util.List;

public interface TicketingService {
    void startTicketOperations();
    void stopTicketOperations();
    void pauseTicketOperations();
    void resumeTicketOperations();
    void addVendor(VendorRequest request, Boolean isInitial);
    void addCustomer(CustomerRequest request);
    SystemStatusResponse getSystemStatus();
    TicketConfiguration configureSystem(TicketConfigurationRequest request);
    TicketConfiguration getCurrentConfig();
    List<SalesOverTimeResponse> getSalesOverTime(LocalDateTime dateTime);
}
