package com.example.jira.controller;

import com.example.jira.model.Categoria;
import com.example.jira.model.Portal;
import com.example.jira.model.Subtopico;
import com.example.jira.service.ChamadoConfigService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/config/chamados")
@RequiredArgsConstructor
public class ConfigChamadoController {

    private final ChamadoConfigService configService;
    
    @GetMapping("/portais")
    public ResponseEntity<List<Portal>> listarPortais() {
        return ResponseEntity.ok(
                configService.listarPortaisDisponiveis()
        );
    }

    @PostMapping("/portais")
    public ResponseEntity<?> criarPortal(
            @RequestParam String nome,
            @RequestParam String descricao) {

        try {
            Portal portal = configService.criarPortal(nome, descricao);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(portal);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    @DeleteMapping("/portais/{id}")
    public ResponseEntity<Void> deletarPortal(
            @PathVariable Long id) {

        configService.deletarPortal(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/portais/{portalId}/categorias")
    public ResponseEntity<List<Categoria>> listarCategoriasPorPortal(
            @PathVariable Long portalId) {

        return ResponseEntity.ok(
                configService.listarCategorias(portalId)
        );
    }

    @PostMapping("/categorias")
    public ResponseEntity<?> criarCategoria(
            @RequestParam String nome,
            @RequestParam Long portalId) {

        try {
            Categoria categoria =
                    configService.criarCategoria(nome, portalId);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(categoria);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    @DeleteMapping("/categorias/{id}")
    public ResponseEntity<Void> deletarCategoria(
            @PathVariable Integer id) {

        configService.deletarCategoria(id);

        return ResponseEntity.noContent().build();
    }
    @GetMapping("/categorias/{categoriaId}/subtopicos")
    public ResponseEntity<List<Subtopico>> listarSubtopicosPorCategoria(
            @PathVariable Integer categoriaId) {

        return ResponseEntity.ok(
                configService.listarSubtopicos(categoriaId)
        );
    }

    @PostMapping("/subtopicos")
    public ResponseEntity<?> criarSubtopico(
            @RequestParam String nome,
            @RequestParam Integer categoriaId) {

        try {
            Subtopico subtopico =
                    configService.criarSubtopico(nome, categoriaId);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(subtopico);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    @DeleteMapping("/subtopicos/{id}")
    public ResponseEntity<Void> deletarSubtopico(
            @PathVariable Integer id) {

        configService.deletarSubtopico(id);

        return ResponseEntity.noContent().build();
    }
}