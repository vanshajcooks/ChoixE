package com.campusconnect.config;

import com.campusconnect.auth.entity.Role;
import com.campusconnect.auth.entity.User;
import com.campusconnect.auth.entity.UserStatus;
import com.campusconnect.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.officer.default-email:officer@campusconnect.edu}")
    private String defaultOfficerEmail;

    @Value("${app.officer.default-password:Officer@CampusConnect2026}")
    private String defaultOfficerPassword;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByRole(Role.PLACEMENT_OFFICER)) {
            User officer = new User(
                    defaultOfficerEmail.trim().toLowerCase(),
                    passwordEncoder.encode(defaultOfficerPassword),
                    "Chief Placement Officer",
                    Role.PLACEMENT_OFFICER,
                    UserStatus.ACTIVE
            );
            userRepository.save(officer);
            log.info("Initialized default Placement Officer account with email: {}", defaultOfficerEmail);
        }
    }
}
