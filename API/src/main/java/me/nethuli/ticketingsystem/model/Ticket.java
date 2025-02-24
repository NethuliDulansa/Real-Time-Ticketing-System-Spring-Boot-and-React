package me.nethuli.ticketingsystem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/*
 * Ticket class represents a ticket that is available for purchase.
 */
@Entity
@Table(name = "tickets")
@Getter
@NoArgsConstructor
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String ticketName;

    @Enumerated(EnumType.STRING)
    @Setter
    private TicketStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Setter
    @Column(name = "sold_at")
    private LocalDateTime soldAt;

    public enum TicketStatus {
        AVAILABLE,
        SOLD
    }

    public Ticket(String ticketName) {
        this.ticketName = ticketName;
        this.status = TicketStatus.AVAILABLE;
        this.createdAt = LocalDateTime.now();
    }

}
