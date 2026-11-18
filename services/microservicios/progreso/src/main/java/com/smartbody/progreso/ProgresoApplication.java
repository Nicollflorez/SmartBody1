package com.smartbody.progreso;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(scanBasePackages = "com.smartbody")
@ConfigurationPropertiesScan("com.smartbody")
public class ProgresoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProgresoApplication.class, args);
    }
}
