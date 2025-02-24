package me.nethuli.ticketingsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * VendorRequest class is used to map the request body of the POST request to /vendors endpoint.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VendorRequest {
    private String name;
    private int releaseRate;
}
