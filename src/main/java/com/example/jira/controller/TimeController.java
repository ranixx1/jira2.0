package com.example.jira.controller;

import com.example.jira.model.Time;
import com.example.jira.repository.TimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/times")
@RequiredArgsConstructor
public class TimeController {

    private final TimeRepository timeRepository;

    @GetMapping
    public ResponseEntity<List<Time>> listarTodos() {
        return ResponseEntity.ok(timeRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Time> buscarPorId(@PathVariable Integer id) {
        return timeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Time> criar(@RequestParam String nome) {
        if (nome == null || nome.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(201).body(timeRepository.save(new Time(nome)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        if (!timeRepository.existsById(id)) return ResponseEntity.notFound().build();
        timeRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{id}/membros")
    public ResponseEntity<Set<Long>> listarMembros(@PathVariable Integer id) {
        return timeRepository.findById(id)
                .map(t -> ResponseEntity.ok(t.getMembros()))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/membros")
    public ResponseEntity<Void> adicionarMembro(
            @PathVariable Integer id,
            @RequestBody Map<String, Long> body) {

        Long userId = body.get("userId");
        if (userId == null) return ResponseEntity.badRequest().build();

        return timeRepository.findById(id).map(time -> {
            time.getMembros().add(userId);
            timeRepository.save(time);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/membros/{userId}")
    public ResponseEntity<Void> removerMembro(
            @PathVariable Integer id,
            @PathVariable Long userId) {

        return timeRepository.findById(id).map(time -> {
            time.getMembros().remove(userId);
            timeRepository.save(time);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}