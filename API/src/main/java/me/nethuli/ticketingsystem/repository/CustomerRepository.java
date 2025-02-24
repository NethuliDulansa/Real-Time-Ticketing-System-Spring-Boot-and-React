package me.nethuli.ticketingsystem.repository;

import me.nethuli.ticketingsystem.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

/*
 * CustomerRepository interface extends JpaRepository interface.
 */
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
