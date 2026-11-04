package com.almacen.producto.dto;

import com.almacen.producto.model.UnidadMedida;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductoRequestDTO(@NotBlank @Size(max = 30) String codigo,
        @NotBlank @Size(max = 120) String nombre, @Size(max = 255) String descripcion,
        @Size(max = 60) String categoria, @NotNull UnidadMedida unidadMedida,
        @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal precioUnitario,
        @Min(0) int stockMinimo, @NotNull @Positive Long proveedorId) { }
