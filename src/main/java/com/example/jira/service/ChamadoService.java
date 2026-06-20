package com.example.jira.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.example.jira.enums.Escopo;
import com.example.jira.enums.Prioridade;
import com.example.jira.enums.Status;
import com.example.jira.model.Categoria;
import com.example.jira.model.Chamado;
import com.example.jira.model.Comentario;
import com.example.jira.model.Subtopico;
import com.example.jira.repository.ChamadoRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class ChamadoService {

    private final ChamadoRepository repository;

    public ChamadoService(ChamadoRepository repository) {
        this.repository = repository;
    }

    public Chamado criarChamado(
            Categoria categoria,
            Subtopico subtopico,
            Prioridade prioridade,
            String titulo,
            String descricao,
            Escopo escopo,
            Long userId) {

        Chamado novoChamado = new Chamado(
                categoria,
                subtopico,
                prioridade,
                Status.ABERTO,
                titulo,
                descricao,
                escopo);

        novoChamado.setUserId(userId);
        return repository.save(novoChamado);
    }

    public Chamado buscarChamadoPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Chamado não encontrado"));
    }

    public Chamado fecharChamado(Integer id) {
        Chamado chamado = buscarChamadoPorId(id);
        chamado.fechar();
        return repository.save(chamado);
    }

    public Chamado alterarStatusChamado(Integer id, Status novoStatus) {
        Chamado chamado = buscarChamadoPorId(id);
        chamado.alterarStatus(novoStatus);
        return repository.save(chamado);
    }

    public Chamado adicionarComentario(
            Integer chamadoId,
            String mensagem,
            Long userId,
            String username) {

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

    public List<Chamado> listarChamados() {
        return repository.findAll();
    }

    public List<Chamado> listarChamadoPorStatus(Status status) {
        return repository.findByStatus(status);
    }

    public List<Chamado> listarChamadosPorPrioridade(Prioridade prioridade) {
        return repository.findByPrioridade(prioridade);
    }
}