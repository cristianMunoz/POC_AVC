package com.grupoaval.avc.producer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumenProductosDTO {
    private String idCliente;
    private String nombreCliente;
    private BigDecimal saldoTotalGlobal;
    private List<ProductoDetalleDTO> productos;

    @Data
    @AllArgsConstructor
    public static class ProductoDetalleDTO {
        private String tipo;
        private String sistema;
        private String identificador;
        private String estado;
        private BigDecimal valor; // Nuevo campo para el total por producto
    }
}