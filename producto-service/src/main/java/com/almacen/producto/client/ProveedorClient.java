package com.almacen.producto.client;

import com.almacen.producto.dto.ProveedorDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "proveedor-service")
public interface ProveedorClient {
    @GetMapping("/api/proveedores/{id}") ProveedorDTO obtener(@PathVariable("id") Long id);
}
