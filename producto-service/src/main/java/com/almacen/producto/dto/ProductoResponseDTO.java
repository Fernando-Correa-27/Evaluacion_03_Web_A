package com.almacen.producto.dto;

import com.almacen.producto.model.UnidadMedida;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductoResponseDTO(Long id, String codigo, String nombre, String descripcion, String categoria,
        UnidadMedida unidadMedida, BigDecimal precioUnitario, int stockMinimo, Long proveedorId,
        boolean activo, LocalDateTime fechaCreacion) { }
