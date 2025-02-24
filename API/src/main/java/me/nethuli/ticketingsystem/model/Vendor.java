package me.nethuli.ticketingsystem.model;

import me.nethuli.ticketingsystem.helper.LoggingHelper;
import me.nethuli.ticketingsystem.helper.TicketPool;
import me.nethuli.ticketingsystem.service.WebSocketMessageService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Vendor class that represents a ticket vendor.
 * Vendors release tickets to the ticket pool at a specified rate.
 */
public class Vendor implements Runnable {
    private final TicketPool ticketPool;
    private final int releaseRate;
    private final String vendorName;
    private final WebSocketMessageService webSocketMessageService;
    private boolean isWaitingForCapacity = false;

    public Vendor(TicketPool ticketPool, int releaseRate, String vendorName,
            WebSocketMessageService webSocketMessageService) {
        this.ticketPool = ticketPool;
        this.releaseRate = releaseRate;
        this.vendorName = vendorName;
        this.webSocketMessageService = webSocketMessageService;
    }

    /**
     * Release tickets to the ticket pool.
     * 
     * @param count Number of tickets to release
     * @return List of released tickets
     */
    private List<Ticket> releaseTickets(int count) {
        List<Ticket> newTickets = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            String ticketId = vendorName + "-TKT-" + UUID.randomUUID().toString().substring(0, 8).replaceAll("-", "");
            newTickets.add(new Ticket(ticketId));
        }

        String message = vendorName + " released " + newTickets.size() + " tickets";
        webSocketMessageService.sendLogMessage(message);
        LoggingHelper.info(message);
        return newTickets;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                int remainingTickets = ticketPool.getRemainingTotalTickets();
                if (remainingTickets <= 0) {
                    String message = vendorName + " stopping: No more tickets available";
                    webSocketMessageService.sendLogMessage(message);
                    LoggingHelper.info(message);
                    break;
                }

                if (ticketPool.isPaused()) {
                    Thread.sleep(100);
                    continue;
                }

                // Check if pool is at capacity before releasing tickets
                if (ticketPool.isAtCapacity()) {
                    // Wait until some capacity is available
                    if (!isWaitingForCapacity) {
                        String message = vendorName + " waiting: Pool is at maximum capacity";
                        webSocketMessageService.sendLogMessage(message);
                        LoggingHelper.info(message);
                        isWaitingForCapacity = true;
                    }
                    Thread.sleep(1000);
                    continue;
                }

                // Find number of tickets to release
                int ticketsToRelease = Math.min(releaseRate, remainingTickets);

                // Release tickets
                List<Ticket> newTickets = releaseTickets(ticketsToRelease);

                // Add tickets to pool
                if (!newTickets.isEmpty()) {
                    ticketPool.addTickets(newTickets);
                }

                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            String message = vendorName + " stopped releasing tickets.";
            webSocketMessageService.sendLogMessage(message);
            LoggingHelper.error(message);
            Thread.currentThread().interrupt();
        }

    }
}
