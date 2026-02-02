package com.example.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.backend.model.CompromissoStatus;
import com.example.backend.model.CompromissoTipo;

import lombok.Data;

@Data
public class CompromissoResponseDTO {
    private Long id;
    private String titulo;
    private String descricao;
    private LocalDateTime dataHora;
    private BigDecimal valor;
    private CompromissoTipo tipo;
    private CompromissoStatus status;
    private boolean urgente;
    private String username; // Only return username, not full user entity
}
