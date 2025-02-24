package me.nethuli.ticketingsystem.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This class is used to read the default properties from the
 * application.properties file.
 * The properties are read from the application.properties file using the prefix
 * "ticketing.default".
 * The properties are then stored in the fields of this class.
 */
@Configuration
@ConfigurationProperties(prefix = "ticketing.default")
@Data
public class TicketingDefaultProperties {
    private int totalTickets = 100;
    private int ticketReleaseRate = 5;
    private int customerRetrievalRate = 7;
    private int maxTicketCapacity = 27;
}
