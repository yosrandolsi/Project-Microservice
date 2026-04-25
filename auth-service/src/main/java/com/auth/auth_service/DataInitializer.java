package com.auth.auth_service;



import com.auth.auth_service.entity.User;
import com.auth.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (userRepository.findByUsername("admin").isEmpty()) {

            User user = new User();
            user.setUsername("admin");
            user.setPassword(passwordEncoder.encode("1234"));
            user.setRole("ROLE_ADMIN");

            userRepository.save(user);
        }
    }
}