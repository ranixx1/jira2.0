package com.example.jira.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import com.example.jira.dto.ChamadoRequest;
import com.example.jira.enums.*;
import com.example.jira.model.Chamado;
import com.example.jira.service.ChamadoService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/chamados")
@RequiredArgsConstructor
public class ChamadoController {

    private final ChamadoService chamadoService;

    @PostMapping
    public ResponseEntity<Chamado> criarChamado(
            @RequestBody ChamadoRequest request,
            Authentication authentication) {

        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long userId = jwt.getClaim("userId");

        Chamado chamado = chamadoService.criarChamado(
                request.getCategoria(),
                request.getSubtopico(),
                request.getPrioridade(),
                request.getTitulo(),
                request.getDescricao(),
                request.getEscopo(),
                userId
        );

        return ResponseEntity.status(201).body(chamado);
    }

    @PostMapping("/{id}/comentarios")
    public ResponseEntity<Chamado> comentar(
            @PathVariable Integer id,
            @RequestBody String mensagem,
            Authentication authentication) {

        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long userId = jwt.getClaim("userId");
        String username = jwt.getClaim("username");

        return ResponseEntity.ok(
                chamadoService.adicionarComentario(
                        id,
                        mensagem,
                        userId,
                        username
                )
        );
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
                chamadoService.alterarStatusChamado(id, novoStatus)
        );
    }

    @GetMapping
    public ResponseEntity<List<Chamado>> listarTodos() {
        return ResponseEntity.ok(chamadoService.listarChamados());
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<Chamado>> listarPorTipo(@PathVariable Tipo tipo) {
        return ResponseEntity.ok(chamadoService.listarChamadosPorTipo(tipo));
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