package me.nethuli.ticketingsystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.nethuli.ticketingsystem.helper.LoggingHelper;
import me.nethuli.ticketingsystem.helper.TicketPool;
import me.nethuli.ticketingsystem.service.WebSocketMessageService;

/*
 * Customer class represents a customer who purchases tickets from the ticket pool.
 */
@Entity
@Table(name = "customers")
@Getter
@NoArgsConstructor(force = true)
public class Customer implements Runnable, Comparable<Customer> {
    @Id //pk
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private final String name;

    @Column(name = "is_vip") // columns hdnw
    private final boolean isVip;

    @Column(name = "no_of_tickets")
    private final Integer noOfTickets;

    @Transient
    private final TicketPool ticketPool;

    @Column(name = "retrieval_rate")
    private int retrievalRate;

    @Transient
    private final WebSocketMessageService webSocketMessageService;

    public Customer(String name, boolean isVip, Integer noOfTickets, TicketPool ticketPool, int retrievalRate,
            WebSocketMessageService webSocketMessageService) {
        this.name = name;
        this.isVip = isVip;
        this.ticketPool = ticketPool;
        this.retrievalRate = retrievalRate;
        this.noOfTickets = noOfTickets;
        this.webSocketMessageService = webSocketMessageService;
    }

    // Compare customers based on their VIP status
    @Override
    public int compareTo(Customer other) {
        // VIP customers have higher priority
        if (this.isVip && !other.isVip)
            return -1;
        if (!this.isVip && other.isVip)
            return 1;
        return 0;
    }

    // Run method to purchase tickets
    @Override
    public void run() {
        try {
            // Purchase tickets until the thread is interrupted
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (ticketPool.isSystemStopped()) {
                        String message = name + " stopped: System has been shut down.";
                        webSocketMessageService.sendLogMessage(message);
                        LoggingHelper.info(message);
                        break;
                    }

                    if (ticketPool.isPaused()) {
                        Thread.sleep(100);
                        continue;
                    }
                    Ticket ticket = ticketPool.removeTicket(this);
                    if (ticket != null) {
                        String message = String.format(
                                "%s%s purchased ticket: %s (Tickets in pool: %d, Total remaining: %d)",
                                name,
                                isVip ? "(VIP)" : "",
                                ticket.getTicketName(),
                                ticketPool.getTicketCount(),
                                ticketPool.getRemainingTotalTickets());
                        webSocketMessageService.sendLogMessage(message);
                        LoggingHelper.info(message);
                    }
                    if (noOfTickets != null) {
                        retrievalRate = noOfTickets;
                    }

                    Thread.sleep(1000 / retrievalRate);
                } catch (InterruptedException e) {
                    String message = name + " stopped purchasing tickets.";
                    webSocketMessageService.sendLogMessage(message);
                    LoggingHelper.error(message);
                    throw e;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public boolean isVip() {
        return isVip;
    }
}
