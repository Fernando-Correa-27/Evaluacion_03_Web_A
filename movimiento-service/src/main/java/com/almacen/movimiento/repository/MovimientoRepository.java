package com.almacen.movimiento.repository;

import com.almacen.movimiento.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MovimientoRepository extends JpaRepository<Movimiento,Long> {
    List<Movimiento> findByProductoIdOrderByFechaDesc(Long productoId);
    @Query("select m from Movimiento m where (:productoId is null or m.productoId=:productoId) and (:tipo is null or m.tipo=:tipo) and (:desde is null or m.fecha>=:desde) and (:hasta is null or m.fecha<=:hasta) order by m.fecha desc")
    List<Movimiento> filtrar(@Param("productoId") Long productoId,@Param("tipo") TipoMovimiento tipo,@Param("desde") LocalDateTime desde,@Param("hasta") LocalDateTime hasta);
    @Query("select coalesce(sum(case when m.tipo=com.almacen.movimiento.model.TipoMovimiento.ENTRADA then m.cantidad else 0 end),0) from Movimiento m where m.productoId=:productoId")
    long entradas(@Param("productoId") Long productoId);
    @Query("select coalesce(sum(case when m.tipo=com.almacen.movimiento.model.TipoMovimiento.SALIDA then m.cantidad else 0 end),0) from Movimiento m where m.productoId=:productoId")
    long salidas(@Param("productoId") Long productoId);
    @Query("select m.productoId as productoId, sum(case when m.tipo=com.almacen.movimiento.model.TipoMovimiento.ENTRADA then m.cantidad else 0 end) as entradas, sum(case when m.tipo=com.almacen.movimiento.model.TipoMovimiento.SALIDA then m.cantidad else 0 end) as salidas from Movimiento m group by m.productoId")
    List<ResumenProducto> resumenStock();
    interface ResumenProducto { Long getProductoId(); Long getEntradas(); Long getSalidas(); }
}
