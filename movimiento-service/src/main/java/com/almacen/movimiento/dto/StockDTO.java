package com.almacen.movimiento.dto;
public record StockDTO(Long productoId,String nombreProducto,long totalEntradas,long totalSalidas,long stockActual,int stockMinimo,boolean bajoMinimo) { }
