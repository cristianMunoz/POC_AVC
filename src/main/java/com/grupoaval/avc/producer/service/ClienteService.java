package com.grupoaval.avc.producer.service;

import com.grupoaval.avc.producer.dto.ResumenProductosDTO;
import com.grupoaval.avc.producer.model.ClienteEntity;
import com.grupoaval.avc.producer.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository repository;

    public ClienteService(ClienteRepository repository) {
        this.repository = repository;
    }

    public ResumenProductosDTO generarResumen(String idCliente) {
        ClienteEntity cliente = repository.findById(idCliente)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        List<ResumenProductosDTO.ProductoDetalleDTO> productos = new ArrayList<>();
        BigDecimal saldoTotalGlobal = BigDecimal.ZERO;

        // 1. FlexCube
        if (cliente.getSaldoFlexcube() != null) {
            productos.add(new ResumenProductosDTO.ProductoDetalleDTO(
                    "Cuenta Ahorros", "FlexCube", "AHO-" + idCliente, "Activa", cliente.getSaldoFlexcube()));
            saldoTotalGlobal = saldoTotalGlobal.add(cliente.getSaldoFlexcube());
        }

        // 2. iSeries (Calcula estado basado en valor real)
        if (cliente.getPrestamoIseries() != null) {
            String estado = cliente.getPrestamoIseries().compareTo(BigDecimal.ZERO) > 0 ? "MORA" : "AL DÍA";
            productos.add(new ResumenProductosDTO.ProductoDetalleDTO(
                    "Préstamo", "iSeries", "PREST-" + idCliente, estado, cliente.getPrestamoIseries()));
            saldoTotalGlobal = saldoTotalGlobal.subtract(cliente.getPrestamoIseries());
        }

        // 3. Thought Machine
        if (cliente.getTarjetaId() != null) {
            BigDecimal valor = cliente.getSaldoThoughtMachine() != null ? cliente.getSaldoThoughtMachine() : BigDecimal.ZERO;
            productos.add(new ResumenProductosDTO.ProductoDetalleDTO(
                    "Tarjeta Crédito", "Thought Machine", cliente.getTarjetaId(), "Vigente", valor));
            saldoTotalGlobal = saldoTotalGlobal.add(valor);
        }

        // 4. SOAP
        if (cliente.getPolizaId() != null) {
            BigDecimal valor = cliente.getSaldoSoap() != null ? cliente.getSaldoSoap() : BigDecimal.ZERO;
            productos.add(new ResumenProductosDTO.ProductoDetalleDTO(
                    "Seguro Vida", "SOAP-Seguros", cliente.getPolizaId(), "Asegurado", valor));
            saldoTotalGlobal = saldoTotalGlobal.add(valor);
        }

        return new ResumenProductosDTO(idCliente, cliente.getNombre(), saldoTotalGlobal, productos);
    }
}