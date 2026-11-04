package com.almacen.producto.controller;

import com.almacen.producto.dto.*;
import com.almacen.producto.services.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {
    private final ProductoService service;
    public ProductoController(ProductoService service) { this.service=service; }
    @GetMapping public List<ProductoResponseDTO> listar(@RequestParam(defaultValue="true") boolean activo,@RequestParam(required=false) Long proveedorId) { return service.listar(activo,proveedorId); }
    @GetMapping("/{id}") public ProductoResponseDTO obtener(@PathVariable Long id) { return service.obtener(id); }
    @PostMapping public ResponseEntity<ProductoResponseDTO> crear(@Valid @RequestBody ProductoRequestDTO dto) { var creado=service.crear(dto); return ResponseEntity.created(URI.create("/api/productos/"+creado.id())).body(creado); }
    @PutMapping("/{id}") public ProductoResponseDTO actualizar(@PathVariable Long id,@Valid @RequestBody ProductoRequestDTO dto) { return service.actualizar(id,dto); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> desactivar(@PathVariable Long id) { service.desactivar(id); return ResponseEntity.noContent().build(); }
}
