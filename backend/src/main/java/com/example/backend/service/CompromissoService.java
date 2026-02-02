package com.example.backend.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.dto.CompromissoRequestDTO;
import com.example.backend.dto.CompromissoResponseDTO;
import com.example.backend.exception.AcessoNegadoException;
import com.example.backend.exception.RecursoNaoEncontradoException;
import com.example.backend.model.Compromisso;
import com.example.backend.model.CompromissoStatus;
import com.example.backend.model.CompromissoTipo;
import com.example.backend.model.Usuario;
import com.example.backend.repository.CompromissoRepository;
import com.example.backend.repository.UsuarioRepository;

@Service
public class CompromissoService {

    @Autowired
    private CompromissoRepository compromissoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<CompromissoResponseDTO> listarTodos(String username) {
        return compromissoRepository.findByUsuarioUsername(username).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CompromissoResponseDTO criar(CompromissoRequestDTO dto, String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado: " + username));

        Compromisso compromisso = toEntity(dto);
        compromisso.setUsuario(usuario);

        Compromisso salvo = compromissoRepository.save(compromisso);
        return toDTO(salvo);
    }

    @Transactional
    public CompromissoResponseDTO atualizar(Long id, CompromissoRequestDTO dto, String username) {
        Compromisso compromisso = buscarPorIdEValidarPropriedade(id, username);
        
        atualizarDados(compromisso, dto);
        
        Compromisso salvo = compromissoRepository.save(compromisso);
        return toDTO(salvo);
    }

    @Transactional
    public void deletar(Long id, String username) {
        Compromisso compromisso = buscarPorIdEValidarPropriedade(id, username);
        compromissoRepository.delete(compromisso);
    }

    // Helper Methods

    private Compromisso buscarPorIdEValidarPropriedade(Long id, String username) {
        Compromisso compromisso = compromissoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Compromisso não encontrado com ID: " + id));

        if (!compromisso.getUsuario().getUsername().equals(username)) {
            throw new AcessoNegadoException("Você não tem permissão para acessar este recurso.");
        }
        return compromisso;
    }

    private void atualizarDados(Compromisso target, CompromissoRequestDTO source) {
        target.setTitulo(source.getTitulo());
        target.setDescricao(source.getDescricao());
        target.setDataHora(source.getDataHora());
        target.setValor(source.getValor() != null ? source.getValor().doubleValue() : 0.0);
        target.setTipo(source.getTipo().name());
        target.setStatus(source.getStatus().name());
        target.setUrgente(source.isUrgente());
    }

    private CompromissoResponseDTO toDTO(Compromisso entity) {
        CompromissoResponseDTO dto = new CompromissoResponseDTO();
        dto.setId(entity.getId());
        dto.setTitulo(entity.getTitulo());
        dto.setDescricao(entity.getDescricao());
        dto.setDataHora(entity.getDataHora());
        dto.setValor(BigDecimal.valueOf(entity.getValor()));
        
        try {
            dto.setTipo(CompromissoTipo.valueOf(entity.getTipo()));
        } catch (IllegalArgumentException e) {
            dto.setTipo(CompromissoTipo.OUTROS); // Fallback
        }

        try {
            dto.setStatus(CompromissoStatus.valueOf(entity.getStatus()));
        } catch (IllegalArgumentException e) {
            dto.setStatus(CompromissoStatus.PENDENTE); // Fallback
        }

        dto.setUrgente(entity.isUrgente());
        dto.setUsername(entity.getUsuario().getUsername());
        return dto;
    }

    private Compromisso toEntity(CompromissoRequestDTO dto) {
        Compromisso entity = new Compromisso();
        entity.setTitulo(dto.getTitulo());
        entity.setDescricao(dto.getDescricao());
        entity.setDataHora(dto.getDataHora());
        entity.setValor(dto.getValor() != null ? dto.getValor().doubleValue() : 0.0);
        entity.setTipo(dto.getTipo() != null ? dto.getTipo().name() : CompromissoTipo.OUTROS.name());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus().name() : CompromissoStatus.PENDENTE.name());
        entity.setUrgente(dto.isUrgente());
        return entity;
    }
}
