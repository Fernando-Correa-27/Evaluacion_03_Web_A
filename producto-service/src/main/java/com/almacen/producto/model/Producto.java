package com.almacen.producto.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "producto", indexes = @Index(name = "idx_producto_proveedor", columnList = "proveedor_id"))
public class Producto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 30) private String codigo;
    @Column(nullable = false, length = 120) private String nombre;
    @Column(length = 255) private String descripcion;
    @Column(length = 60) private String categoria;
    @Enumerated(EnumType.STRING) @Column(name = "unidad_medida", nullable = false, length = 20) private UnidadMedida unidadMedida;
    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2) private BigDecimal precioUnitario;
    @Column(name = "stock_minimo", nullable = false) private int stockMinimo;
    @Column(name = "proveedor_id", nullable = false) private Long proveedorId;
    @Column(nullable = false) private boolean activo = true;
    @Column(name = "fecha_creacion", nullable = false, updatable = false, columnDefinition = "DATETIME") private LocalDateTime fechaCreacion;
    @PrePersist void alCrear() { if (fechaCreacion == null) fechaCreacion = LocalDateTime.now(); }
    public Long getId() { return id; } public void setId(Long v) { id = v; }
    public String getCodigo() { return codigo; } public void setCodigo(String v) { codigo = v; }
    public String getNombre() { return nombre; } public void setNombre(String v) { nombre = v; }
    public String getDescripcion() { return descripcion; } public void setDescripcion(String v) { descripcion = v; }
    public String getCategoria() { return categoria; } public void setCategoria(String v) { categoria = v; }
    public UnidadMedida getUnidadMedida() { return unidadMedida; } public void setUnidadMedida(UnidadMedida v) { unidadMedida = v; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; } public void setPrecioUnitario(BigDecimal v) { precioUnitario = v; }
    public int getStockMinimo() { return stockMinimo; } public void setStockMinimo(int v) { stockMinimo = v; }
    public Long getProveedorId() { return proveedorId; } public void setProveedorId(Long v) { proveedorId = v; }
    public boolean isActivo() { return activo; } public void setActivo(boolean v) { activo = v; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; } public void setFechaCreacion(LocalDateTime v) { fechaCreacion = v; }
}
