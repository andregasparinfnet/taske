package com.example.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

import com.example.backend.dto.CompromissoRequestDTO;
import com.example.backend.dto.CompromissoResponseDTO;
import com.example.backend.service.CompromissoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/compromissos")
public class CompromissoController {

    @Autowired
    private CompromissoService service;

    @GetMapping
    public List<CompromissoResponseDTO> listar(java.security.Principal principal) {
        return service.listarTodos(principal.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompromissoResponseDTO criar(@RequestBody @Valid CompromissoRequestDTO dto, java.security.Principal principal) {
        return service.criar(dto, principal.getName());
    }

    @PutMapping("/{id}")
    public CompromissoResponseDTO atualizar(@PathVariable Long id, @RequestBody @Valid CompromissoRequestDTO dto, java.security.Principal principal) {
        return service.atualizar(id, dto, principal.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable Long id, java.security.Principal principal) {
        try {
            service.deletar(id, principal.getName());
        } catch (com.example.backend.exception.RecursoNaoEncontradoException e) {
            // Idempotent delete: if resource doesn't exist, we consider it deleted.
        }
    }

    @GetMapping("/export")
    public void exportar(
            @RequestParam(name = "format", defaultValue = "csv") String format,
            java.security.Principal principal,
            HttpServletResponse response
    ) throws IOException {
        if (!"csv".equalsIgnoreCase(format)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato não suportado: " + format);
        }

        var lista = service.listarTodos(principal.getName());

        String filename = URLEncoder.encode("compromissos-" + principal.getName() + ".csv", StandardCharsets.UTF_8);
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        try (PrintWriter writer = response.getWriter()) {
            writer.println("id,titulo,dataHora,tipo,status,valor,urgente,descricao");
            for (var c : lista) {
                String csvTitulo = com.example.backend.util.CsvUtils.sanitize(c.getTitulo());
                String csvDescricao = com.example.backend.util.CsvUtils.sanitize(c.getDescricao());
                String dataHora = c.getDataHora() != null ? dtf.format(c.getDataHora()) : "";
                String valor = (c.getValor() != null) ? c.getValor().toString() : "";
                String urgente = c.isUrgente() ? "true" : "false";
                writer.printf("%d,%s,%s,%s,%s,%s,%s,%s%n",
                        c.getId(),
                        csvTitulo,
                        dataHora,
                        c.getTipo() != null ? c.getTipo().name() : "",
                        c.getStatus() != null ? c.getStatus().name() : "",
                        valor,
                        urgente,
                        csvDescricao
                );
            }
            writer.flush();
        }
    }

}
