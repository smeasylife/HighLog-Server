package goatHeaven.highLog.config;

import goatHeaven.highLog.domain.Role;
import goatHeaven.highLog.jooq.tables.pojos.Users;
import goatHeaven.highLog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Value("${admin.name:관리자}")
    private String adminName;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!userRepository.existsByEmail(adminEmail)) {
            Users admin = new Users();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setName(adminName);
            admin.setRole(Role.ADMIN.name());
            admin.setMarketingAgreement(false);

            userRepository.insert(admin);
            log.info("Admin account created: {}", adminEmail);
        } else {
            log.info("Admin account already exists: {}", adminEmail);
        }
    }
}
