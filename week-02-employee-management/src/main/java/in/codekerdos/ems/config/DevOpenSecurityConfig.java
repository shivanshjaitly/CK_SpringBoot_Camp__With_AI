package in.codekerdos.ems.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Week 3 Exercise E — activate with: spring.profiles.active=dev-open,h2
 * Debugging only. Do not use in production.
 */
@Configuration
@Profile("dev-open")
public class DevOpenSecurityConfig {

    @Bean
    @Order(0)
    public SecurityFilterChain openSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable());
        return http.build();
    }
}
