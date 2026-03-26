package com.datacenter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot application entry point for DataCenter Interactive Mapping System.
 * Enables scheduling for periodic data ingestion and provides REST API endpoints
 * for data center queries, search, analytics, and real-time updates.
 */
@SpringBootApplication
@EnableScheduling
public class DataCenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataCenterApplication.class, args);
    }
}