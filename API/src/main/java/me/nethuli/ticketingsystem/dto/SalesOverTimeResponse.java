package me.nethuli.ticketingsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/*
 * This class is used to represent the response of the sales over time.
 */
@AllArgsConstructor
@Getter
public class SalesOverTimeResponse {
    private String time;
    private long salesCount;
}
