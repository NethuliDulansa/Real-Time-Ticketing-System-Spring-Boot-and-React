package me.nethuli.ticketingsystem.helper;

import lombok.Getter;
import lombok.Setter;

/*
 * TicketConfiguration class is used to store the configuration details of the ticketing system.
 */
@Setter
@Getter
public class TicketConfiguration {
    private int totalTickets;
    // Ticket release rate per second
    private int ticketReleaseRate;
    // Ticket retrieval rate per second
    private int customerRetrievalRate;
    private int maxTicketCapacity;

    public TicketConfiguration(int totalTickets, int ticketReleaseRate, int customerRetrievalRate,
            int maxTicketCapacity) {
        this.totalTickets = totalTickets;
        this.ticketReleaseRate = ticketReleaseRate;
        this.customerRetrievalRate = customerRetrievalRate;
        this.maxTicketCapacity = maxTicketCapacity;
    }

}
