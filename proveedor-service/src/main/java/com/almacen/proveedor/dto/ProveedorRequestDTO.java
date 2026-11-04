package com.almacen.proveedor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProveedorRequestDTO(
        @NotBlank @Size(max = 20) String documento,
        @NotBlank @Size(max = 120) String razonSocial,
        @Size(max = 100) String contacto,
        @Size(max = 20) String telefono,
        @Email @Size(max = 100) String email,
        @Size(max = 200) String direccion) { }
