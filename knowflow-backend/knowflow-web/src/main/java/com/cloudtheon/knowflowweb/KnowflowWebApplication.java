package com.cloudtheon.knowflowweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.cloudtheon")
public class KnowflowWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(KnowflowWebApplication.class, args);
    }

}
