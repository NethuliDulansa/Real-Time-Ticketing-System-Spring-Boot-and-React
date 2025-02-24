package me.nethuli.ticketingsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/*
 * This class is used to represent the response of the system status.
 */
@Data
@AllArgsConstructor
public class SystemStatusResponse {
    private boolean running;
    private boolean paused;
    private int availableTickets;
    private int remainingTotalTickets;
    private int activeVendors;
    private int activeCustomers;
}
