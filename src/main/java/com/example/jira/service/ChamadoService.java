package com.example.jira.service;

import java.util.List;

import org.springframework.stereotype.Service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.Authentication;
import com.example.jira.enums.Escopo;
import com.example.jira.enums.Prioridade;
import com.example.jira.enums.Tipo;
import com.example.jira.enums.Status;
import com.example.jira.model.Chamado;
import com.example.jira.model.Comentario;
import com.example.jira.repository.ChamadoRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ChamadoService {

    private final ChamadoRepository repository;

    public ChamadoService(ChamadoRepository repository) {
        this.repository = repository;
    }

    // =========================
    // CRIAR CHAMADO (JWT USER)
    // =========================
    public Chamado criarChamado(
            Tipo tipo,
            Prioridade prioridade,
            String titulo,
            String descricao,
            Escopo escopo) {

        Jwt jwt = (Jwt) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Long userId = jwt.getClaim("userId");
        String username = jwt.getClaim("username");

        Chamado novoChamado = new Chamado(
                tipo,
                prioridade,
                Status.ABERTO,
                titulo,
                descricao,
                escopo);

        novoChamado.setUserId(userId);
        return repository.save(novoChamado);
    }

    // =========================
    // BUSCAR POR ID
    // =========================
    public Chamado buscarChamadoPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Chamado não encontrado"));
    }

    // =========================
    // FECHAR CHAMADO
    // =========================
    public Chamado fecharChamado(Integer id) {
        Chamado chamado = buscarChamadoPorId(id);

        chamado.fechar();

        return repository.save(chamado);
    }

    // =========================
    // ALTERAR STATUS
    // =========================
    public Chamado alterarStatusChamado(Integer id, Status novoStatus) {
        Chamado chamado = buscarChamadoPorId(id);

        chamado.alterarStatus(novoStatus);

        return repository.save(chamado);
    }

    // =========================
    // ADICIONAR COMENTÁRIO (JWT USER)
    // =========================
    public Chamado adicionarComentario(
            Integer chamadoId,
            String mensagem) {

        Jwt jwt = (Jwt) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        Long userId = jwt.getClaim("userId");
        String username = jwt.getClaim("username");

        Chamado chamado = buscarChamadoPorId(chamadoId);

        if (chamado.getStatus() == Status.FECHADO) {
            throw new IllegalStateException(
                    "Não é possível adicionar comentários a um chamado fechado.");
        }

        Comentario comentario = new Comentario(
                mensagem,
                userId,
                username,
                chamado);

        chamado.adicionarComentario(comentario);

        return repository.save(chamado);
    }


    // =========================
    // LISTAR TODOS
    // =========================
    public List<Chamado> listarChamados() {
        return repository.findAll();
    }

    // =========================
    // FILTROS
    // =========================
    public List<Chamado> listarChamadosPorTipo(Tipo tipo) {
        return repository.findByTipo(tipo);
    }

    public List<Chamado> listarChamadoPorStatus(Status status) {
        return repository.findByStatus(status);
    }

    public List<Chamado> listarChamadosPorPrioridade(Prioridade prioridade) {
        return repository.findByPrioridade(prioridade);
    }

    public List<Chamado> listarPorCriador(Long userId) {
        return repository.findByUserId(userId);
    }
}