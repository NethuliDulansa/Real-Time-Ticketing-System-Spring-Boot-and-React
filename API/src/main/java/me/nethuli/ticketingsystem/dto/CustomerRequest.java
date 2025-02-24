package me.nethuli.ticketingsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CustomerRequest class is used to represent the request body of the customer
 * request. This class is used to map the request body to a Java object.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerRequest {
    private String name;
    private boolean vip;
    private Integer numberOfTickets;
    private int retrievalRate;
}
