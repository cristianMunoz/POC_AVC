package com.grupoaval.avc.producer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Desactiva CSRF globalmente para facilitar la POC y la consola H2
                .csrf(AbstractHttpConfigurer::disable)
                // Permite el uso de frames para que la interfaz de H2 cargue correctamente
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .authorizeHttpRequests(auth -> auth
                        // Acceso libre a la consola y a los endpoints de prueba
                        .requestMatchers("/h2-console/**").permitAll()
                        .anyRequest().permitAll()
                );
        return http.build();
    }
}