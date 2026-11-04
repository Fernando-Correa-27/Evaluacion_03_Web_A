package com.almacen.movimiento.client;
import com.almacen.movimiento.dto.ProductoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
@FeignClient(name="producto-service")
public interface ProductoClient { @GetMapping("/api/productos/{id}") ProductoDTO obtener(@PathVariable("id") Long id); }
