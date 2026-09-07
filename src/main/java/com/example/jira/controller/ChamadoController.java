package com.example.jira.controller;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import com.example.jira.dto.*;
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
        
        ChamadoResponseDTO dto = chamadoService.criarChamado(
                request,
                extrairUserId(authentication),
                extrairUsername(authentication)
        );
        return ResponseEntity.status(201).body(dto);
    }

    @GetMapping("/meus")
    public ResponseEntity<List<ChamadoResponseDTO>> listarMeus(Authentication authentication) {
        List<ChamadoResponseDTO> dtos = chamadoService
                .listarChamadosPorUsuario(extrairUserId(authentication))
                .stream()
                .map(ChamadoResponseDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/{id}/comentarios")
    public ResponseEntity<ChamadoResponseDTO> comentar(
            @PathVariable Integer id,
            @Valid @RequestBody ComentarioRequestDTO request,
            Authentication authentication) {
        
        Chamado chamado = chamadoService.adicionarComentario(
                id,
                request.mensagem(),
                extrairUserId(authentication),
                extrairUsername(authentication),
                extrairRole(authentication)
        );
        return ResponseEntity.ok(ChamadoResponseDTO.from(chamado));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<ChamadoResponseDTO> buscarPorId(
            @PathVariable Integer id,
            Authentication authentication) {
        
        Chamado chamado = chamadoService.buscarChamadoVisivel(
                id, 
                extrairUserId(authentication),
                extrairRole(authentication)
        );
        return ResponseEntity.ok(ChamadoResponseDTO.from(chamado));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ChamadoResponseDTO> alterarStatus(
            @PathVariable Integer id,
            @Valid @RequestBody AlterarStatusRequestDTO request,
            Authentication authentication) {
        
        Chamado chamado = chamadoService.alterarStatus(
                id,
                request.status(),
                extrairUserId(authentication),
                extrairUsername(authentication),
                extrairRole(authentication)
        );
        return ResponseEntity.ok(ChamadoResponseDTO.from(chamado));
    }

    @GetMapping
    public ResponseEntity<List<ChamadoResponseDTO>> listarTodos(Authentication authentication) {
        List<ChamadoResponseDTO> dtos = chamadoService.listarChamados(
                extrairUserId(authentication),
                extrairRole(authentication)
        )
                .stream()
                .map(ChamadoResponseDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ChamadoResponseDTO>> listarPorStatus(
            @PathVariable Status status,
            Authentication authentication) {
        
        List<ChamadoResponseDTO> dtos = chamadoService
                .listarChamadoPorStatus(
                        status, 
                        extrairUserId(authentication),
                        extrairRole(authentication)
                )
                .stream()
                .map(ChamadoResponseDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/prioridade/{prioridade}")
    public ResponseEntity<List<ChamadoResponseDTO>> listarPorPrioridade(
            @PathVariable Prioridade prioridade,
            Authentication authentication) {
        
        List<ChamadoResponseDTO> dtos = chamadoService
                .listarChamadosPorPrioridade(
                        prioridade, 
                        extrairUserId(authentication),
                        extrairRole(authentication)
                )
                .stream()
                .map(ChamadoResponseDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}/historico")
    public ResponseEntity<List<ChamadoHistoricoDTO>> listarHistorico(@PathVariable Integer id) {
        return ResponseEntity.ok(chamadoService.listarHistorico(id));
    }

    private Long extrairUserId(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getClaim("userId");
    }

    private String extrairUsername(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getSubject();
    }

    private String extrairRole(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getClaimAsString("role");
    }
}