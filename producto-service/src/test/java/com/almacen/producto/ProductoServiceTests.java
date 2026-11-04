package com.almacen.producto;

import com.almacen.producto.client.ProveedorClient;
import com.almacen.producto.dto.*;
import com.almacen.producto.exception.UnprocessableException;
import com.almacen.producto.model.UnidadMedida;
import com.almacen.producto.repository.ProductoRepository;
import com.almacen.producto.services.ProductoService;
import org.junit.jupiter.api.Test;import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTests {
    @Mock ProductoRepository repository; @Mock ProveedorClient proveedorClient; @InjectMocks ProductoService service;
    private ProductoRequestDTO dto(){return new ProductoRequestDTO("SKU-1","Teclado",null,"Periféricos",UnidadMedida.UNIDAD,new BigDecimal("25.00"),3,1L);}
    @Test void creaProductoConProveedorActivo(){when(proveedorClient.obtener(1L)).thenReturn(new ProveedorDTO(1L,"DOC-1","Proveedor",true));when(repository.existsByCodigo("SKU-1")).thenReturn(false);when(repository.save(any())).thenAnswer(i->i.getArgument(0));assertEquals("SKU-1",service.crear(dto()).codigo());}
    @Test void rechazaProveedorInactivo(){when(proveedorClient.obtener(1L)).thenReturn(new ProveedorDTO(1L,"DOC-1","Proveedor",false));assertThrows(UnprocessableException.class,()->service.crear(dto()));verify(repository,never()).save(any());}
}
