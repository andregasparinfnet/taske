package com.example.backend.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class ModelTest {

    @Test
    void testUsuarioModel() {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setUsername("user");
        u.setPassword("pass");

        assertEquals(1L, u.getId());
        assertEquals("user", u.getUsername());
        assertEquals("pass", u.getPassword());
        
        Usuario u2 = new Usuario();
        u2.setUsername("user2");
        u2.setPassword("pass2");
        assertEquals("user2", u2.getUsername());
        
        // Teste de equals/hashcode/toString se necessário (Lombok @Data gera)
        assertNotNull(u.toString());
        assertNotEquals(u, u2);
    }

    @Test
    void testCompromissoModel() {
        Compromisso c = new Compromisso();
        c.setId(1L);
        c.setTitulo("Titulo");
        c.setDescricao("Desc");
        c.setTipo("TIPO");
        c.setStatus("PENDENTE");
        c.setValor(100.0);
        c.setUrgente(true);
        LocalDateTime now = LocalDateTime.now();
        c.setDataHora(now);
        
        Usuario u = new Usuario();
        c.setUsuario(u);

        assertEquals(1L, c.getId());
        assertEquals("Titulo", c.getTitulo());
        assertEquals("Desc", c.getDescricao());
        assertEquals("TIPO", c.getTipo());
        assertEquals("PENDENTE", c.getStatus());
        assertEquals(100.0, c.getValor());
        assertTrue(c.isUrgente());
        assertEquals(now, c.getDataHora());
        assertEquals(u, c.getUsuario());
        
        assertNotNull(c.toString());
    }
}
