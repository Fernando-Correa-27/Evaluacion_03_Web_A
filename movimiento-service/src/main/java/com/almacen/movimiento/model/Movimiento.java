package com.almacen.movimiento.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="movimiento", indexes=@Index(name="idx_movimiento_producto_fecha",columnList="producto_id,fecha"))
public class Movimiento {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="producto_id",nullable=false) private Long productoId;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=10) private TipoMovimiento tipo;
    @Column(nullable=false) private int cantidad;
    @Column(nullable=false,columnDefinition="DATETIME") private LocalDateTime fecha;
    @Column(length=255) private String observacion;
    @PrePersist void alCrear(){if(fecha==null)fecha=LocalDateTime.now();}
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public Long getProductoId(){return productoId;} public void setProductoId(Long v){productoId=v;}
    public TipoMovimiento getTipo(){return tipo;} public void setTipo(TipoMovimiento v){tipo=v;}
    public int getCantidad(){return cantidad;} public void setCantidad(int v){cantidad=v;}
    public LocalDateTime getFecha(){return fecha;} public void setFecha(LocalDateTime v){fecha=v;}
    public String getObservacion(){return observacion;} public void setObservacion(String v){observacion=v;}
}
