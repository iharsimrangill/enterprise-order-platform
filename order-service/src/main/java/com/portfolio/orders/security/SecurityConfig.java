package com.portfolio.orders.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health/**").permitAll()
                        .requestMatchers("/actuator/info").permitAll()
                        .requestMatchers("/actuator/**").hasRole("OPS")
                        .requestMatchers(HttpMethod.POST, "/api/v1/orders", "/api/v1/orders/**").hasRole("ORDER_WRITER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/orders", "/api/v1/orders/**").authenticated()
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(
            PasswordEncoder passwordEncoder,
            @Value("${app.security.reader.username:order-reader}") String readerUsername,
            @Value("${app.security.reader.password:reader-change-me}") String readerPassword,
            @Value("${app.security.writer.username:order-writer}") String writerUsername,
            @Value("${app.security.writer.password:writer-change-me}") String writerPassword,
            @Value("${app.security.ops.username:ops-user}") String opsUsername,
            @Value("${app.security.ops.password:ops-change-me}") String opsPassword) {

        return new InMemoryUserDetailsManager(
                User.withUsername(readerUsername)
                        .password(passwordEncoder.encode(readerPassword))
                        .roles("ORDER_READER")
                        .build(),

                User.withUsername(writerUsername)
                        .password(passwordEncoder.encode(writerPassword))
                        .roles("ORDER_READER", "ORDER_WRITER")
                        .build(),

                User.withUsername(opsUsername)
                        .password(passwordEncoder.encode(opsPassword))
                        .roles("OPS")
                        .build());
    }
}
