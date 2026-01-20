package com.example.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.model.Compromisso;

@Repository
public interface CompromissoRepository extends JpaRepository<Compromisso, Long> {
}
