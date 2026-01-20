package com.example.backend;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Permite que o React acesse este backend (evita erro de CORS)
public class HelloController {

    @GetMapping("/status")
    public String checkStatus() {
        return "LifeOS: Sistema Operacional Online e Pronto!";
    }
}
