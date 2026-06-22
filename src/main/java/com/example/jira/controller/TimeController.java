package com.example.jira.controller;

import com.example.jira.model.Time;
import com.example.jira.repository.TimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/times")
@RequiredArgsConstructor
public class TimeController {

    private final TimeRepository timeRepository;

    @GetMapping
    public ResponseEntity<List<Time>> listarTodos() {
        return ResponseEntity.ok(timeRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Time> criar(@RequestParam String nome) {
        if (nome == null || nome.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        Time time = new Time(nome);
        return ResponseEntity.status(201).body(timeRepository.save(time));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        if (!timeRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        timeRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}