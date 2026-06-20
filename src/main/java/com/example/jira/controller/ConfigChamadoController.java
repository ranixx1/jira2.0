package com.example.jira.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import com.example.jira.model.Categoria;
import com.example.jira.model.Subtopico;
import com.example.jira.repository.CategoriaRepository;
import com.example.jira.repository.SubtopicoRepository;
import com.example.jira.repository.TimeRepository;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RestController
@RequestMapping("/config/chamados")
@RequiredArgsConstructor
public class ConfigChamadoController {

    private final CategoriaRepository categoriaRepository;
    private final SubtopicoRepository subtopicoRepository;
    private final TimeRepository timeRepository;

    @GetMapping("/disponiveis")
    public ResponseEntity<List<Categoria>> listarDisponiveis(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        List<Integer> timesIds = jwt.getClaim("timesIds");
        return ResponseEntity.ok(categoriaRepository.findByTimesIds(timesIds));
    }

    @PostMapping("/categorias")
    public ResponseEntity<?> criarCategoria(@RequestParam String nome, @RequestParam Integer timeId) {
        return timeRepository.findById(timeId).map(time -> {
            Categoria novaCategoria = new Categoria(nome,time);
            return ResponseEntity.status(201).body(categoriaRepository.save(novaCategoria));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/subtopicos")
    public ResponseEntity<?> criarSubtopico(@RequestParam String nome, @RequestParam Integer categoriaId) {
        return categoriaRepository.findById(categoriaId).map(categoria -> {
            Subtopico novoSubtopico = new Subtopico(nome, categoria);
            return ResponseEntity.status(201).body(subtopicoRepository.save(novoSubtopico));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/categorias/{id}")
    public ResponseEntity<Void> deletarCategoria(@PathVariable Integer id) {
        if (categoriaRepository.existsById(id)) {
            categoriaRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/subtopicos/{id}")
    public ResponseEntity<Void> deletarSubtopico(@PathVariable Integer id) {
        if (subtopicoRepository.existsById(id)) {
            subtopicoRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}