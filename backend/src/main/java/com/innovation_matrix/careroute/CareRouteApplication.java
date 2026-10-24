package com.innovation_matrix.careroute;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan
public class CareRouteApplication {

    public static void main(String[] args) {
        SpringApplication.run(CareRouteApplication.class, args);
    }
}
