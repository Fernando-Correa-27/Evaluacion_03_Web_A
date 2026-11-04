package com.almacen.movimiento;

import com.almacen.movimiento.dto.MovimientoResponseDTO;
import com.almacen.movimiento.model.TipoMovimiento;
import com.almacen.movimiento.services.MovimientoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = com.almacen.movimiento.controller.MovimientoController.class, properties={"spring.cloud.config.enabled=false","eureka.client.enabled=false"})
class MovimientoControllerTests {
    @Autowired MockMvc mvc;
    @MockitoBean MovimientoService service;

    @Test void cantidadCeroResponde400YCampo() throws Exception {
        mvc.perform(post("/api/movimientos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"productoId\":1,\"tipo\":\"ENTRADA\",\"cantidad\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0].field").value("cantidad"));
        verifyNoInteractions(service);
    }

    @Test void entradaValidaResponde201() throws Exception {
        when(service.crear(any())).thenReturn(new MovimientoResponseDTO(7L,1L,TipoMovimiento.ENTRADA,5,LocalDateTime.parse("2026-11-04T10:30:00"),null));
        mvc.perform(post("/api/movimientos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"productoId\":1,\"tipo\":\"ENTRADA\",\"cantidad\":5}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location","/api/movimientos/7"))
                .andExpect(jsonPath("$.cantidad").value(5));
    }
}
