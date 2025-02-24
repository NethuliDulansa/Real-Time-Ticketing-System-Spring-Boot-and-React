package me.nethuli.ticketingsystem.repository;

import me.nethuli.ticketingsystem.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/*
 * TicketRepository is an interface that extends JpaRepository interface.
 */
public interface TicketRepository extends JpaRepository<Ticket, Long> {
        // Custom query to find tickets by status
        List<Ticket> findByStatus(Ticket.TicketStatus status);

        // Custom query to find tickets by status and soldAt
        @Query("""
                        SELECT NEW map(FORMATDATETIME(t.soldAt, 'yyyy-MM-dd HH:mm:ss') as timestamp, COUNT(t) as count)
                        FROM Ticket t
                        WHERE t.status = 'SOLD'
                        AND t.soldAt BETWEEN :startTime AND :endTime
                        GROUP BY FORMATDATETIME(t.soldAt, 'yyyy-MM-dd HH:mm:ss')
                        ORDER BY timestamp
                        """)
        List<Map<String, Object>> findTicketsSoldPerSecond(
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);

        // Custom query to count available tickets
        @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = 'AVAILABLE'")
        long countAvailableTickets();
}
