package com.example.jira.controller;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import com.example.jira.dto.ChamadoRequestDTO;
import com.example.jira.dto.ChamadoResponseDTO;
import com.example.jira.dto.ComentarioRequestDTO;
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

    @PostMapping
    public ResponseEntity<ChamadoResponseDTO> criarChamado(
            @Valid @RequestBody ChamadoRequestDTO request,
            Authentication authentication) {

        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long userId = jwt.getClaim("userId");

        ChamadoResponseDTO dto = chamadoService.criarChamado(request, userId);
        return ResponseEntity.status(201).body(dto);
    }

    @GetMapping("/meus")
    public ResponseEntity<List<ChamadoResponseDTO>> listarMeus(
            Authentication authentication) {

        Jwt jwt = (Jwt) authentication.getPrincipal();

        Long userId = jwt.getClaim("userId");

        List<ChamadoResponseDTO> dtos = chamadoService
                .listarChamadosPorUsuario(userId)
                .stream()
                .map(ChamadoResponseDTO::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/{id}/comentarios")
    public ResponseEntity<ChamadoResponseDTO> comentar(
            @PathVariable Integer id,
            @Valid @RequestBody ComentarioRequestDTO req,
            Authentication authentication) {

        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long userId = jwt.getClaim("userId");
        String username = jwt.getClaim("username");

        return ResponseEntity.ok(
                ChamadoResponseDTO.from(
                        chamadoService.adicionarComentario(id, req.mensagem(), userId, username)));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<ChamadoResponseDTO> buscarPorId(@PathVariable Integer id) {
        Chamado chamado = chamadoService.buscarChamadoPorId(id);
        return ResponseEntity.ok(ChamadoResponseDTO.from(chamado));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Chamado> alterarStatus(
            @PathVariable Integer id,
            @RequestBody Status novoStatus) {
        return ResponseEntity.ok(
                chamadoService.alterarStatusChamado(id, novoStatus));
    }

    @GetMapping
    public ResponseEntity<List<ChamadoResponseDTO>> listarTodos() {
        List<ChamadoResponseDTO> dtos = chamadoService.listarChamados()
                .stream()
                .map(ChamadoResponseDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ChamadoResponseDTO>> listarPorStatus(@PathVariable Status status) {
        List<ChamadoResponseDTO> dtos = chamadoService.listarChamadoPorStatus(status)
                .stream()
                .map(ChamadoResponseDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/prioridade/{prioridade}")
    public ResponseEntity<List<ChamadoResponseDTO>> listarPorPrioridade(@PathVariable Prioridade prioridade) {
        List<ChamadoResponseDTO> dtos = chamadoService.listarChamadosPorPrioridade(prioridade)
                .stream()
                .map(ChamadoResponseDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}