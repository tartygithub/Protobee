package com.example.docprocessor;

import com.example.docprocessor.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DocProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocProcessorApplication.class, args);
    }

    @Bean
    public CommandLineRunner initDefaultUser(UserService userService) {
        return args -> {
            if (userService.findByUsername("admin").isEmpty()) {
                userService.registerUser("admin", "admin123");
            }
        };
    }
}
