package com.example.backend.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UsuarioTest {

    @Test
    void deveTestarGettersESetters() {
        // Cenário
        Usuario usuario = new Usuario();

        // Ação
        usuario.setId(1L);
        usuario.setUsername("teste@user.com");
        usuario.setPassword("senhaSecreta");

        // Verificação
        assertEquals(1L, usuario.getId());
        assertEquals("teste@user.com", usuario.getUsername());
        assertEquals("senhaSecreta", usuario.getPassword());
    }

    @Test
    void deveRetornarValoresNulosParaNovaInstancia() {
        // Cenário
        Usuario usuario = new Usuario();

        // Verificação
        assertNull(usuario.getId());
        assertNull(usuario.getUsername());
        assertNull(usuario.getPassword());
    }
}
