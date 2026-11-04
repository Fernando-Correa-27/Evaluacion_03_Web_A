package com.almacen.proveedor;

import com.almacen.proveedor.services.ProveedorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers=com.almacen.proveedor.controller.ProveedorController.class, properties={"spring.cloud.config.enabled=false","eureka.client.enabled=false"})
class ProveedorControllerTests {
    @Autowired MockMvc mvc;
    @MockitoBean ProveedorService service;
    @Test void correoInvalidoDevuelve400YCampo() throws Exception {
        mvc.perform(post("/api/proveedores").contentType(MediaType.APPLICATION_JSON)
                .content("{\"documento\":\"DOC-1\",\"razonSocial\":\"Proveedor\",\"email\":\"no-es-correo\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("email"));
        verifyNoInteractions(service);
    }
}
