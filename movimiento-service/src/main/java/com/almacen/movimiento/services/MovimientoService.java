package com.almacen.movimiento.services;

import com.almacen.movimiento.client.ProductoClient;
import com.almacen.movimiento.dto.*;
import com.almacen.movimiento.exception.*;
import com.almacen.movimiento.mapper.MovimientoMapper;
import com.almacen.movimiento.model.*;
import com.almacen.movimiento.repository.MovimientoRepository;
import feign.FeignException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MovimientoService {
    private final MovimientoRepository repository; private final ProductoClient productoClient;
    public MovimientoService(MovimientoRepository repository,ProductoClient productoClient){this.repository=repository;this.productoClient=productoClient;}
    @Transactional public MovimientoResponseDTO crear(MovimientoRequestDTO dto){
        ProductoDTO producto=obtenerProducto(dto.productoId());
        if(!producto.activo())throw new UnprocessableException("El producto está inactivo");
        if(dto.tipo()==TipoMovimiento.SALIDA){long disponible=repository.entradas(dto.productoId())-repository.salidas(dto.productoId());if(dto.cantidad()>disponible)throw new UnprocessableException("Stock insuficiente. Disponible: "+disponible);}
        return MovimientoMapper.aRespuesta(repository.save(MovimientoMapper.aEntidad(dto)));
    }
    @Transactional(readOnly=true) public List<MovimientoResponseDTO> listar(Long productoId,TipoMovimiento tipo,LocalDateTime desde,LocalDateTime hasta){
        if(desde!=null&&hasta!=null&&hasta.isBefore(desde))throw new IllegalArgumentException("hasta debe ser igual o posterior a desde");
        return repository.filtrar(productoId,tipo,desde,hasta).stream().map(MovimientoMapper::aRespuesta).toList();
    }
    @Transactional(readOnly=true) public MovimientoResponseDTO obtener(Long id){return repository.findById(id).map(MovimientoMapper::aRespuesta).orElseThrow(()->new ResourceNotFoundException("No existe el movimiento "+id));}
    @Transactional(readOnly=true) public List<MovimientoResponseDTO> historial(Long productoId){obtenerProducto(productoId);return repository.findByProductoIdOrderByFechaDesc(productoId).stream().map(MovimientoMapper::aRespuesta).toList();}
    @Transactional(readOnly=true) public StockDTO stock(Long productoId){ProductoDTO p=obtenerProducto(productoId);long entradas=repository.entradas(productoId),salidas=repository.salidas(productoId),actual=entradas-salidas;return new StockDTO(productoId,p.nombre(),entradas,salidas,actual,p.stockMinimo(),actual<p.stockMinimo());}
    @Transactional(readOnly=true) public List<StockDTO> stockTodos(){return repository.resumenStock().stream().map(r->{ProductoDTO p=obtenerProducto(r.getProductoId());long actual=r.getEntradas()-r.getSalidas();return new StockDTO(r.getProductoId(),p.nombre(),r.getEntradas(),r.getSalidas(),actual,p.stockMinimo(),actual<p.stockMinimo());}).toList();}
    private ProductoDTO obtenerProducto(Long id){try{return productoClient.obtener(id);}catch(FeignException e){if(e.status()==404)throw new ResourceNotFoundException("No existe el producto "+id);throw new ServiceUnavailableException("No fue posible consultar producto-service");}}
}
