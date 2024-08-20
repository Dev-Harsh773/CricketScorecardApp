package com.cricket.scorecard.config;

import com.cricket.scorecard.model.User;
import com.cricket.scorecard.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByUsername("HarshShringi").isEmpty()) {
            User admin = new User();
            admin.setUsername("HarshShringi");
            admin.setPassword(passwordEncoder.encode("Harsh1234"));
            admin.setRole("ADMIN");
            userRepository.save(admin);
            System.out.println("Default admin user HarshShringi seeded successfully.");
        }
    }
}
