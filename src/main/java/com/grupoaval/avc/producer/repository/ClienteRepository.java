package com.grupoaval.avc.producer.repository;

import com.grupoaval.avc.producer.model.ClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<ClienteEntity, String> {
}