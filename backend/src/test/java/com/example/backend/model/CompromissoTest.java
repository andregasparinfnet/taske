package com.example.backend.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class CompromissoTest {

    @Test
    void deveTestarGettersESetters() {
        // Cenário
        Compromisso compromisso = new Compromisso();
        LocalDateTime dataHora = LocalDateTime.of(2026, 2, 15, 10, 30);
        Usuario usuario = new Usuario();
        usuario.setUsername("teste@user.com");

        // Ação
        compromisso.setId(1L);
        compromisso.setTitulo("Reunião");
        compromisso.setDataHora(dataHora);
        compromisso.setTipo("TRABALHO");
        compromisso.setStatus("PENDENTE");
        compromisso.setValor(150.50);
        compromisso.setDescricao("Reunião importante");
        compromisso.setUrgente(true);
        compromisso.setUsuario(usuario);

        // Verificação
        assertEquals(1L, compromisso.getId());
        assertEquals("Reunião", compromisso.getTitulo());
        assertEquals(dataHora, compromisso.getDataHora());
        assertEquals("TRABALHO", compromisso.getTipo());
        assertEquals("PENDENTE", compromisso.getStatus());
        assertEquals(150.50, compromisso.getValor());
        assertEquals("Reunião importante", compromisso.getDescricao());
        assertTrue(compromisso.isUrgente());
        assertEquals(usuario, compromisso.getUsuario());
    }

    @Test
    void deveRetornarValoresPadraoParaNovaInstancia() {
        // Cenário
        Compromisso compromisso = new Compromisso();

        // Verificação
        assertNull(compromisso.getId());
        assertNull(compromisso.getTitulo());
        assertNull(compromisso.getDataHora());
        assertNull(compromisso.getTipo());
        assertEquals("PENDENTE", compromisso.getStatus());
        assertEquals(0.0, compromisso.getValor());
        assertNull(compromisso.getDescricao());
        assertFalse(compromisso.isUrgente());
        assertNull(compromisso.getUsuario());
    }

    @Test
    void deveRetornarFalseQuandoUrgenteForNull() {
        // Cenário
        Compromisso compromisso = new Compromisso();
        compromisso.setUrgente(null);

        // Verificação
        assertFalse(compromisso.isUrgente());
    }

    @Test
    void deveRetornarTrueQuandoUrgenteForTrue() {
        // Cenário
        Compromisso compromisso = new Compromisso();
        compromisso.setUrgente(true);

        // Verificação
        assertTrue(compromisso.isUrgente());
    }
}
