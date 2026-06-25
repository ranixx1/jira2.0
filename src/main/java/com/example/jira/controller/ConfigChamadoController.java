package com.example.jira.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.example.jira.model.Categoria;
import com.example.jira.model.Subtopico;
import com.example.jira.service.ChamadoConfigService;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RestController
@RequestMapping("/config/chamados")
@RequiredArgsConstructor
public class ConfigChamadoController {

    private final ChamadoConfigService configService;

    @GetMapping("/disponiveis")
    public ResponseEntity<List<Categoria>> listarDisponiveis(Authentication authentication) {
        return ResponseEntity.ok(configService.listarDisponiveis());
    }

    @PostMapping("/categorias")
    public ResponseEntity<?> criarCategoria(@RequestParam String nome, @RequestParam Integer timeId) {
        try {
            Categoria novaCategoria = configService.criarCategoria(nome, timeId);
            return ResponseEntity.status(HttpStatus.CREATED).body(novaCategoria);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/subtopicos")
    public ResponseEntity<?> criarSubtopico(@RequestParam String nome, @RequestParam Integer categoriaId) {
        try {
            Subtopico novoSubtopico = configService.criarSubtopico(nome, categoriaId);
            return ResponseEntity.status(HttpStatus.CREATED).body(novoSubtopico);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/categorias/{id}")
    public ResponseEntity<Void> deletarCategoria(@PathVariable Integer id) {
        try {
            configService.deletarCategoria(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/subtopicos/{id}")
    public ResponseEntity<Void> deletarSubtopico(@PathVariable Integer id) {
        try {
            configService.deletarSubtopico(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}