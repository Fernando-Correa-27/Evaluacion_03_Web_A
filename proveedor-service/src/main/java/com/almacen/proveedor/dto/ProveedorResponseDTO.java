package com.almacen.proveedor.dto;

import java.time.LocalDateTime;

public record ProveedorResponseDTO(Long id, String documento, String razonSocial, String contacto,
        String telefono, String email, String direccion, boolean activo, LocalDateTime fechaCreacion) { }
