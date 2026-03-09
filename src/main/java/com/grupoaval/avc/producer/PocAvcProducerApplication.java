package com.grupoaval.avc.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PocAvcProducerApplication {

	public static void main(String[] args) {
		SpringApplication.run(PocAvcProducerApplication.class, args);
	}

	// Definición manual para solucionar la falta del bean ObjectMapper
	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}
}