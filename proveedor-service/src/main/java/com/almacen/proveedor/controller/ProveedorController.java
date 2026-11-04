package com.almacen.proveedor.controller;

import com.almacen.proveedor.dto.*;
import com.almacen.proveedor.services.ProveedorService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/proveedores")
public class ProveedorController {
    private final ProveedorService service;
    public ProveedorController(ProveedorService service) { this.service = service; }
    @GetMapping public List<ProveedorResponseDTO> listar(@RequestParam(defaultValue = "true") boolean activo) { return service.listar(activo); }
    @GetMapping("/{id}") public ProveedorResponseDTO obtener(@PathVariable Long id) { return service.obtener(id); }
    @PostMapping public ResponseEntity<ProveedorResponseDTO> crear(@Valid @RequestBody ProveedorRequestDTO dto) {
        ProveedorResponseDTO creado = service.crear(dto);
        return ResponseEntity.created(URI.create("/api/proveedores/" + creado.id())).body(creado);
    }
    @PutMapping("/{id}") public ProveedorResponseDTO actualizar(@PathVariable Long id, @Valid @RequestBody ProveedorRequestDTO dto) { return service.actualizar(id, dto); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> desactivar(@PathVariable Long id) { service.desactivar(id); return ResponseEntity.noContent().build(); }
}
