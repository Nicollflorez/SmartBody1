package com.smartbody.planes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(scanBasePackages = "com.smartbody")
@ConfigurationPropertiesScan("com.smartbody")
public class PlanesApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlanesApplication.class, args);
    }
}
