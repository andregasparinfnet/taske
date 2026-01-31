package com.example.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.backend.model.Compromisso;
import com.example.backend.repository.CompromissoRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/compromissos")
@CrossOrigin(origins = "*")
public class CompromissoController {

    @Autowired
    private CompromissoRepository repository;

    @Autowired
    private com.example.backend.repository.UsuarioRepository usuarioRepository;

    @GetMapping
    public List<Compromisso> listar(java.security.Principal principal) {
        return repository.findByUsuarioUsername(principal.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Compromisso criar(@RequestBody @Valid Compromisso compromisso, java.security.Principal principal) {
        var usuario = usuarioRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário não encontrado"));
        
        compromisso.setUsuario(usuario);
        return repository.save(compromisso);
    }

    @PutMapping("/{id}")
    public Compromisso atualizar(@PathVariable Long id, @RequestBody @Valid Compromisso compromisso, java.security.Principal principal) {
        return repository.findById(id)
                .map(record -> {
                    // Security Check: Is Owner?
                    if (!record.getUsuario().getUsername().equals(principal.getName())) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado ao recurso");
                    }
                    
                    record.setTitulo(compromisso.getTitulo());
                    record.setDataHora(compromisso.getDataHora());
                    record.setTipo(compromisso.getTipo());
                    record.setUrgente(compromisso.isUrgente());
                    record.setStatus(compromisso.getStatus());
                    record.setValor(compromisso.getValor());
                    record.setDescricao(compromisso.getDescricao());
                    return repository.save(record);
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Compromisso não encontrado"));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id, java.security.Principal principal) {
        repository.findById(id).ifPresent(record -> {
            if (!record.getUsuario().getUsername().equals(principal.getName())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado ao recurso");
            }
            repository.deleteById(id);
        });
    }
}
