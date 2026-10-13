package com.smartbody.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/** Microservicio de autenticacion y cuenta de usuario. Unico emisor de JWT del sistema. */
@SpringBootApplication(scanBasePackages = "com.smartbody")
@ConfigurationPropertiesScan("com.smartbody")
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
