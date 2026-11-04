package com.almacen.producto.mapper;

import com.almacen.producto.dto.*;
import com.almacen.producto.model.Producto;

public final class ProductoMapper {
    private ProductoMapper() { }
    public static ProductoResponseDTO aRespuesta(Producto p) { return new ProductoResponseDTO(p.getId(), p.getCodigo(), p.getNombre(), p.getDescripcion(), p.getCategoria(), p.getUnidadMedida(), p.getPrecioUnitario(), p.getStockMinimo(), p.getProveedorId(), p.isActivo(), p.getFechaCreacion()); }
    public static void aplicar(ProductoRequestDTO d, Producto p) { p.setCodigo(d.codigo()); p.setNombre(d.nombre()); p.setDescripcion(d.descripcion()); p.setCategoria(d.categoria()); p.setUnidadMedida(d.unidadMedida()); p.setPrecioUnitario(d.precioUnitario()); p.setStockMinimo(d.stockMinimo()); p.setProveedorId(d.proveedorId()); }
}
