package com.example.restaurantapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.util.Collection;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return (request, response, authentication) -> {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            boolean isAdmin = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isStaff = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"));

            if (isAdmin) {
                response.sendRedirect("/");
            } else if (isStaff) {
                response.sendRedirect("/orders");
            } else {
                // Customer redirect to Order Placement POS Page
                response.sendRedirect("/orders/new");
            }
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
            )
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/h2-console/**", "/login", "/register", "/save-user", "/menu", "/images/**", "/uploads/**", "/css/**", "/js/**", "/webjars/**").permitAll()
                .requestMatchers("/orders/new", "/orders/create", "/orders/{id}", "/orders/{id}/checkout", "/orders/{id}/pay", "/ai-assistant/**").hasAnyRole("ADMIN", "STAFF", "CUSTOMER", "USER")
                .requestMatchers("/orders").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers("/", "/categories/**", "/add-food", "/save-food", "/edit-food/**", "/delete-food/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(customSuccessHandler())
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }
}
