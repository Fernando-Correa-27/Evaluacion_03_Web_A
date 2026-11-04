package com.almacen.movimiento.dto;
import com.almacen.movimiento.model.TipoMovimiento;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
public record MovimientoRequestDTO(@NotNull @Positive Long productoId,@NotNull TipoMovimiento tipo,
        @NotNull @Positive Integer cantidad,LocalDateTime fecha,@Size(max=255) String observacion) { }
