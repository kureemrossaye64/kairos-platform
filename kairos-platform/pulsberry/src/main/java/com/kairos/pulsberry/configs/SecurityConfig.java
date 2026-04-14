package com.kairos.pulsberry.configs;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kairos.pulsberry.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
	
    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;

    private final JwtAuthenticationFilter jwtAuthFilter;
    
    private final ObjectMapper mapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // Define public and protected endpoints
            .authorizeHttpRequests(req -> req
                // Make auth endpoints, health checks, and swagger public
            		.requestMatchers("/api/v1/auth/**").permitAll() // Authentication controller
                    .requestMatchers("/actuator/health").permitAll() // Health checks
                    
                    // --- Frontend Static Assets ---
                    // Allow access to the SPA's entry point and static assets (CSS, JS, images, etc.)
                    .requestMatchers("/", "/index.html", "/static/**", "/favicon.ico", "/manifest.json", "/logo192.png").permitAll()
                    
                    // --- Catch-all for SPA routing ---
                    // Allow requests to any path that does not contain a file extension.
                    // This lets React Router handle the route. Our SpaController will forward these.
                    .requestMatchers("/**/{path:[^\\.]*}").permitAll()

                    // --- Secure Everything Else ---
                    // Any other request (especially to our API) MUST be authenticated.
                    .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Add our custom JWT filter before the standard authentication filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // NEVER use List.of("*") for allowedOrigins in production
        configuration.setAllowedOrigins(List.of(allowedOrigins));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}