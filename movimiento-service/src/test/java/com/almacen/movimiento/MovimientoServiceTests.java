package com.almacen.movimiento;

import com.almacen.movimiento.client.ProductoClient;import com.almacen.movimiento.dto.*;import com.almacen.movimiento.exception.UnprocessableException;import com.almacen.movimiento.model.TipoMovimiento;import com.almacen.movimiento.repository.MovimientoRepository;import com.almacen.movimiento.services.MovimientoService;
import org.junit.jupiter.api.Test;import org.junit.jupiter.api.extension.ExtendWith;import org.mockito.*;import org.mockito.junit.jupiter.MockitoExtension;import static org.junit.jupiter.api.Assertions.*;import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovimientoServiceTests {
    @Mock MovimientoRepository repository; @Mock ProductoClient productoClient; @InjectMocks MovimientoService service;
    @Test void registraEntrada(){when(productoClient.obtener(1L)).thenReturn(new ProductoDTO(1L,"SKU","Arroz",5,true));when(repository.save(any())).thenAnswer(i->{var m=(com.almacen.movimiento.model.Movimiento)i.getArgument(0);m.setId(4L);return m;});var saved=service.crear(new MovimientoRequestDTO(1L,TipoMovimiento.ENTRADA,10,null,"Compra"));assertEquals(4L,saved.id());assertEquals(10,saved.cantidad());}
    @Test void rechazaSalidaMayorQueStock(){when(productoClient.obtener(1L)).thenReturn(new ProductoDTO(1L,"SKU","Arroz",5,true));when(repository.entradas(1L)).thenReturn(3L);when(repository.salidas(1L)).thenReturn(1L);assertThrows(UnprocessableException.class,()->service.crear(new MovimientoRequestDTO(1L,TipoMovimiento.SALIDA,5,null,null)));verify(repository,never()).save(any());}
    @Test void calculaStockActual(){when(productoClient.obtener(1L)).thenReturn(new ProductoDTO(1L,"SKU","Arroz",5,true));when(repository.entradas(1L)).thenReturn(12L);when(repository.salidas(1L)).thenReturn(4L);var stock=service.stock(1L);assertEquals(8,stock.stockActual());assertFalse(stock.bajoMinimo());}
}
