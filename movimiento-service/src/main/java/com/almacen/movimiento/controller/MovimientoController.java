package com.almacen.movimiento.controller;

import com.almacen.movimiento.dto.*;
import com.almacen.movimiento.model.TipoMovimiento;
import com.almacen.movimiento.services.MovimientoService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.net.URI;import java.time.LocalDateTime;import java.util.List;

@RestController @RequestMapping("/api/movimientos")
public class MovimientoController {
    private final MovimientoService service;
    public MovimientoController(MovimientoService service){this.service=service;}
    @PostMapping public ResponseEntity<MovimientoResponseDTO> crear(@Valid @RequestBody MovimientoRequestDTO dto){var creado=service.crear(dto);return ResponseEntity.created(URI.create("/api/movimientos/"+creado.id())).body(creado);}
    @GetMapping public List<MovimientoResponseDTO> listar(@RequestParam(required=false) Long productoId,@RequestParam(required=false) TipoMovimiento tipo,@RequestParam(required=false) LocalDateTime desde,@RequestParam(required=false) LocalDateTime hasta){return service.listar(productoId,tipo,desde,hasta);}
    @GetMapping("/{id}") public MovimientoResponseDTO obtener(@PathVariable Long id){return service.obtener(id);}
    @GetMapping("/producto/{productoId}") public List<MovimientoResponseDTO> historial(@PathVariable Long productoId){return service.historial(productoId);}
    @GetMapping("/producto/{productoId}/stock") public StockDTO stock(@PathVariable Long productoId){return service.stock(productoId);}
    @GetMapping("/stock") public List<StockDTO> stockTodos(){return service.stockTodos();}
}
