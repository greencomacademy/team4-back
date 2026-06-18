package com.deliveryinsider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class DeliveryInsiderApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeliveryInsiderApplication.class, args);
    }

}
