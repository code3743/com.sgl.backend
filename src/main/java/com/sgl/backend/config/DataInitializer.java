package com.sgl.backend.config;


import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.sgl.backend.entity.Role;
import com.sgl.backend.entity.User;
import com.sgl.backend.repository.RoleRepository;
import com.sgl.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        createRoleIfNotExists("ADMIN");
        createRoleIfNotExists("MONITOR");
        createRoleIfNotExists("DOCENTE");
        createRoleIfNotExists("ESTUDIANTE");

        if (!userRepository.existsById("admin_code")) {
            Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
            String hashedPassword = passwordEncoder.encode("admin_password");
            User admin = User.builder()
                    .code("admin_code")
                    .name("Admin User")
                    .email("admin@correounivalle.edu.co")
                    .document("123456789")
                    .password(hashedPassword)
                    .role(adminRole)
                    .build();
            userRepository.save(admin);
        }
    }

    private void createRoleIfNotExists(String name) {
        if (roleRepository.findByName(name).isEmpty()) {
            Role role = Role.builder().name(name).build();
            roleRepository.save(role);
        }
    }
}
