package com.hcmcyu.auth.config;

import com.hcmcyu.auth.entity.Role;
import com.hcmcyu.auth.entity.UserAccount;
import com.hcmcyu.auth.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevDataSeeder implements CommandLineRunner {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean enabled;
    private final String username;
    private final String email;
    private final String password;

    public DevDataSeeder(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            @Value("${auth.seed.ward-secretary.enabled:false}") boolean enabled,
            @Value("${auth.seed.ward-secretary.username}") String username,
            @Value("${auth.seed.ward-secretary.email}") String email,
            @Value("${auth.seed.ward-secretary.password}") String password
    ) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (!enabled || userAccountRepository.existsByUsername(username)) {
            return;
        }

        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setEmail(email.trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(Role.WARD_SECRETARY);
        user.setEnabled(true);
        userAccountRepository.save(user);
    }
}
