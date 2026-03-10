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
        // Buscamos el cliente. Si no existe, devolvemos un objeto vacío para evitar el 500.
        ClienteEntity cliente = repository.findById(idCliente)
                .orElse(null);

        if (cliente == null) {
            return new ResumenProductosDTO(idCliente, "Cliente no registrado", BigDecimal.ZERO, new ArrayList<>());
        }

        List<ResumenProductosDTO.ProductoDetalleDTO> productos = new ArrayList<>();
        BigDecimal saldoTotalGlobal = BigDecimal.ZERO;

        // 1. FlexCube (Solo si tiene saldo > 0)
        if (cliente.getSaldoFlexcube() != null && cliente.getSaldoFlexcube().compareTo(BigDecimal.ZERO) > 0) {
            productos.add(new ResumenProductosDTO.ProductoDetalleDTO(
                    "Cuenta Ahorros", "FlexCube", "AHO-" + idCliente, "Activa", cliente.getSaldoFlexcube()));
            saldoTotalGlobal = saldoTotalGlobal.add(cliente.getSaldoFlexcube());
        }

        // 2. iSeries (Solo si tiene deuda > 0)
        if (cliente.getPrestamoIseries() != null && cliente.getPrestamoIseries().compareTo(BigDecimal.ZERO) > 0) {
            productos.add(new ResumenProductosDTO.ProductoDetalleDTO(
                    "Préstamo Personal", "iSeries", "PREST-" + idCliente, "MORA", cliente.getPrestamoIseries()));
            saldoTotalGlobal = saldoTotalGlobal.subtract(cliente.getPrestamoIseries());
        }

        // 3. Thought Machine (Solo si tiene deuda > 0)
        if (cliente.getSaldoTarjeta() != null && cliente.getSaldoTarjeta().compareTo(BigDecimal.ZERO) > 0) {
            productos.add(new ResumenProductosDTO.ProductoDetalleDTO(
                    "Tarjeta de Crédito", "Thought Machine", "TDC-" + idCliente, "Activa", cliente.getSaldoTarjeta()));
            saldoTotalGlobal = saldoTotalGlobal.subtract(cliente.getSaldoTarjeta());
        }

        // 4. SOAP (Solo si tiene valor > 0)
        if (cliente.getSaldoSeguros() != null && cliente.getSaldoSeguros().compareTo(BigDecimal.ZERO) > 0) {
            productos.add(new ResumenProductosDTO.ProductoDetalleDTO(
                    "Seguro de Vida", "SOAP", "POL-" + idCliente, "Vigente", cliente.getSaldoSeguros()));
            saldoTotalGlobal = saldoTotalGlobal.add(cliente.getSaldoSeguros());
        }

        return new ResumenProductosDTO(idCliente, cliente.getNombre(), saldoTotalGlobal, productos);
    }
}