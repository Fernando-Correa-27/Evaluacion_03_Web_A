package com.almacen.producto;

import com.almacen.producto.services.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers=com.almacen.producto.controller.ProductoController.class, properties={"spring.cloud.config.enabled=false","eureka.client.enabled=false"})
class ProductoControllerTests {
    @Autowired MockMvc mvc;
    @MockitoBean ProductoService service;
    @Test void precioNegativoDevuelve400YCampo() throws Exception {
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"codigo\":\"SKU-1\",\"nombre\":\"Teclado\",\"unidadMedida\":\"UNIDAD\",\"precioUnitario\":-1,\"stockMinimo\":0,\"proveedorId\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("precioUnitario"));
        verifyNoInteractions(service);
    }
}
