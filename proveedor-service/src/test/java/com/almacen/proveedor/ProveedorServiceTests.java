package com.almacen.proveedor;

import com.almacen.proveedor.dto.ProveedorRequestDTO;
import com.almacen.proveedor.exception.ConflictException;
import com.almacen.proveedor.model.Proveedor;
import com.almacen.proveedor.repository.ProveedorRepository;
import com.almacen.proveedor.services.ProveedorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProveedorServiceTests {
    @Mock ProveedorRepository repository;
    @InjectMocks ProveedorService service;

    @Test void creaProveedor() {
        var request = new ProveedorRequestDTO("20123456789", "Distribuidora Uno", null, null, "ventas@example.com", null);
        when(repository.existsByDocumento(request.documento())).thenReturn(false);
        when(repository.save(any(Proveedor.class))).thenAnswer(invocation -> { Proveedor p = invocation.getArgument(0); p.setId(1L); return p; });
        assertEquals(1L, service.crear(request).id());
    }

    @Test void rechazaDocumentoDuplicado() {
        var request = new ProveedorRequestDTO("20123456789", "Distribuidora Uno", null, null, null, null);
        when(repository.existsByDocumento(request.documento())).thenReturn(true);
        assertThrows(ConflictException.class, () -> service.crear(request));
        verify(repository, never()).save(any());
    }

    @Test void eliminaConBajaLogica() {
        Proveedor p = new Proveedor(); p.setId(3L); p.setActivo(true);
        when(repository.findById(3L)).thenReturn(Optional.of(p));
        service.desactivar(3L);
        assertFalse(p.isActivo());
        verify(repository).save(p);
    }
}
