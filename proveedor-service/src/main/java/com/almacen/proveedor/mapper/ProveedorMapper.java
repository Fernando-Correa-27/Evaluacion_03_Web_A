package com.almacen.proveedor.mapper;

import com.almacen.proveedor.dto.ProveedorRequestDTO;
import com.almacen.proveedor.dto.ProveedorResponseDTO;
import com.almacen.proveedor.model.Proveedor;

public final class ProveedorMapper {
    private ProveedorMapper() { }
    public static ProveedorResponseDTO aRespuesta(Proveedor p) {
        return new ProveedorResponseDTO(p.getId(), p.getDocumento(), p.getRazonSocial(), p.getContacto(),
                p.getTelefono(), p.getEmail(), p.getDireccion(), p.isActivo(), p.getFechaCreacion());
    }
    public static void aplicar(ProveedorRequestDTO dto, Proveedor p) {
        p.setDocumento(dto.documento()); p.setRazonSocial(dto.razonSocial()); p.setContacto(dto.contacto());
        p.setTelefono(dto.telefono()); p.setEmail(dto.email()); p.setDireccion(dto.direccion());
    }
}
