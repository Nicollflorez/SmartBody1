package com.smartbody;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SmartbodyApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartbodyApplication.class, args);
    }
}
