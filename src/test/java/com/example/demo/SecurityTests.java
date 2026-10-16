package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class SecurityTests {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void sinTokenDevuelve401() throws Exception {
        mvc.perform(get("/api/pedidos")).andExpect(status().isUnauthorized());
    }

    @Test
    void conTokenYScopeDevuelve200() throws Exception {
        mvc.perform(get("/api/pedidos")
                .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_access_as_user"))))
                .andExpect(status().isOk());
    }

    @Test
    void tokenSinScopeDevuelve403() throws Exception {
        mvc.perform(get("/api/pedidos").with(jwt()))
                .andExpect(status().isForbidden());
    }

    @Test
    void borrarSinRolAdminDevuelve403() throws Exception {
        mvc.perform(delete("/api/pedidos/1")
                .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_access_as_user"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void borrarConRolAdminNoEsProhibido() throws Exception {
        // 404 porque el pedido no existe, lo importante es que NO sea 403
        mvc.perform(delete("/api/pedidos/999")
                .with(jwt().authorities(
                        new SimpleGrantedAuthority("SCOPE_access_as_user"),
                        new SimpleGrantedAuthority("ROLE_Admin"))))
                .andExpect(status().isNotFound());
    }
}
