package com.almacen.movimiento.mapper;
import com.almacen.movimiento.dto.*;
import com.almacen.movimiento.model.Movimiento;
public final class MovimientoMapper {
    private MovimientoMapper(){}
    public static MovimientoResponseDTO aRespuesta(Movimiento m){return new MovimientoResponseDTO(m.getId(),m.getProductoId(),m.getTipo(),m.getCantidad(),m.getFecha(),m.getObservacion());}
    public static Movimiento aEntidad(MovimientoRequestDTO d){Movimiento m=new Movimiento();m.setProductoId(d.productoId());m.setTipo(d.tipo());m.setCantidad(d.cantidad());m.setFecha(d.fecha());m.setObservacion(d.observacion());return m;}
}
