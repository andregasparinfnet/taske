package com.example.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.model.Compromisso;
import com.example.backend.repository.CompromissoRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/compromissos")
@CrossOrigin(origins = "*")
public class CompromissoController {

    @Autowired
    private CompromissoRepository repository;

    @GetMapping
    public List<Compromisso> listar() {
        return repository.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Compromisso criar(@RequestBody @Valid Compromisso compromisso) {
        return repository.save(compromisso);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
