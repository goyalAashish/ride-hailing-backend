package com.ridehailing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the in-memory Ride-Hailing backend.
 * <p>
 * Persistence is intentionally kept in-memory (ConcurrentHashMap-backed repositories) for the
 * scope of this exercise -- no JDBC/JPA auto-configuration is pulled in.
 */
@SpringBootApplication
public class RideHailingApplication {

    public static void main(String[] args) {
        SpringApplication.run(RideHailingApplication.class, args);
    }
}
