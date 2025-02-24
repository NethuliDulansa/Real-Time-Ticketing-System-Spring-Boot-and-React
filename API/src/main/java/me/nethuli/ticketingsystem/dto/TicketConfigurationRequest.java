package me.nethuli.ticketingsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TicketConfigurationRequest is the data transfer object that is used to
 * transfer the ticket configuration data from the client to the server.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketConfigurationRequest {
    private int totalTickets;
    private int ticketReleaseRate;
    private int customerRetrievalRate;
    private int maxTicketCapacity;
}
