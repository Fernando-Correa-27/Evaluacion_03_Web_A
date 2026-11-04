package com.almacen.movimiento.dto;
import com.almacen.movimiento.model.TipoMovimiento;
import java.time.LocalDateTime;
public record MovimientoResponseDTO(Long id,Long productoId,TipoMovimiento tipo,int cantidad,LocalDateTime fecha,String observacion) { }
