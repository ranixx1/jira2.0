package com.example.jira.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import com.example.jira.dto.ChamadoRequestDTO;
import com.example.jira.dto.ChamadoResponseDTO;
import com.example.jira.dto.ComentarioRequest;
import com.example.jira.enums.*;
import com.example.jira.model.Chamado;
import com.example.jira.service.ChamadoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/chamados")
@RequiredArgsConstructor
public class ChamadoController {

    private final ChamadoService chamadoService;

    // ChamadoController.java — resolve as entidades no service
    @PostMapping
    public ResponseEntity<ChamadoResponseDTO> criarChamado(
            @Valid @RequestBody ChamadoRequestDTO request,
            Authentication authentication) {

        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long userId = jwt.getClaim("userId");

        ChamadoResponseDTO dto = chamadoService.criarChamado(request, userId);
        return ResponseEntity.status(201).body(dto);
    }

    @PostMapping("/{id}/comentarios")
    public ResponseEntity<ChamadoResponseDTO> comentar(
            @PathVariable Integer id,
            @Valid @RequestBody ComentarioRequest req,
            Authentication authentication) {

        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long userId = jwt.getClaim("userId");
        String username = jwt.getClaim("username");

        return ResponseEntity.ok(
                ChamadoResponseDTO.from(
                        chamadoService.adicionarComentario(id, req.mensagem(), userId, username)));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Chamado> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(chamadoService.buscarChamadoPorId(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Chamado> alterarStatus(
            @PathVariable Integer id,
            @RequestBody Status novoStatus) {
        return ResponseEntity.ok(
                chamadoService.alterarStatusChamado(id, novoStatus));
    }

    @GetMapping
    public ResponseEntity<List<Chamado>> listarTodos() {
        return ResponseEntity.ok(chamadoService.listarChamados());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Chamado>> listarPorStatus(@PathVariable Status status) {
        return ResponseEntity.ok(chamadoService.listarChamadoPorStatus(status));
    }

    @GetMapping("/prioridade/{prioridade}")
    public ResponseEntity<List<Chamado>> listarPorPrioridade(@PathVariable Prioridade prioridade) {
        return ResponseEntity.ok(chamadoService.listarChamadosPorPrioridade(prioridade));
    }
}