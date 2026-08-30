package com.smartretail.pricemonitor;

import com.smartretail.pricemonitor.config.GeminiProperties;
import com.smartretail.pricemonitor.constants.ApprovalStatus;
import com.smartretail.pricemonitor.constants.RoleName;
import com.smartretail.pricemonitor.entity.Role;
import com.smartretail.pricemonitor.entity.User;
import com.smartretail.pricemonitor.repository.RoleRepository;
import com.smartretail.pricemonitor.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@EnableAsync
@EnableJpaAuditing
@EnableConfigurationProperties(GeminiProperties.class)
@EnableJpaRepositories(
    basePackages = "com.smartretail.pricemonitor.repository",
    excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*\\.mongo\\..*")
)
@EnableMongoRepositories(
    basePackages = "com.smartretail.pricemonitor.repository.mongo"
)
@Slf4j
public class SmartRetailApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartRetailApplication.class, args);
    }

    @Bean
    public CommandLineRunner initDefaultAdminAndRoles(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            GeminiProperties geminiProperties) {
        return args -> {
            boolean isConfigured = geminiProperties.getApiKey() != null && !geminiProperties.getApiKey().trim().isEmpty();
            log.info("Gemini AI configured: {}", isConfigured);
            log.info("Gemini model: {}", geminiProperties.getModel());

            Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                    .orElseGet(() -> roleRepository.save(new Role(null, RoleName.ROLE_ADMIN)));
            Role authRole = roleRepository.findByName(RoleName.ROLE_AUTHORITY)
                    .orElseGet(() -> roleRepository.save(new Role(null, RoleName.ROLE_AUTHORITY)));
            Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                    .orElseGet(() -> roleRepository.save(new Role(null, RoleName.ROLE_USER)));

            User admin = userRepository.findByUsernameOrEmail("admin", "admin@smartretail.com")
                    .orElseGet(() -> User.builder()
                            .username("admin")
                            .email("admin@smartretail.com")
                            .build());

            admin.setPassword(passwordEncoder.encode("password"));
            admin.setRoles(Set.of(adminRole));
            admin.setEnabled(true);
            admin.setIsEmailVerified(true);
            admin.setIsOtpVerified(true);
            admin.setApprovalStatus(ApprovalStatus.ACTIVE);
            userRepository.save(admin);

            User officer = userRepository.findByUsernameOrEmail("officer", "officer@smartretail.com")
                    .orElseGet(() -> User.builder()
                            .username("officer")
                            .email("officer@smartretail.com")
                            .build());

            officer.setPassword(passwordEncoder.encode("password"));
            officer.setRoles(Set.of(authRole));
            officer.setEnabled(true);
            officer.setIsEmailVerified(true);
            officer.setIsOtpVerified(true);
            officer.setApprovalStatus(ApprovalStatus.APPROVED);
            userRepository.save(officer);
        };
    }
}
