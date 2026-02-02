package com.example.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.backend.model.CompromissoStatus;
import com.example.backend.model.CompromissoTipo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompromissoRequestDTO {

    @NotBlank(message = "O título é obrigatório")
    @Size(max = 100, message = "O título deve ter no máximo 100 caracteres")
    private String titulo;

    @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres")
    private String descricao;

    @NotNull(message = "A data e hora são obrigatórias")
    @FutureOrPresent(message = "A data deve ser presente ou futura")
    private LocalDateTime dataHora;

    @DecimalMin(value = "0.0", message = "O valor deve ser positivo")
    private BigDecimal valor;

    @NotNull(message = "O tipo é obrigatório")
    private CompromissoTipo tipo;

    private CompromissoStatus status = CompromissoStatus.PENDENTE;

    private boolean urgente;
}
