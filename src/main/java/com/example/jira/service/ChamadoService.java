package com.example.jira.service;

import java.util.List;
import org.springframework.stereotype.Service;

import com.example.jira.dto.ChamadoRequestDTO;
import com.example.jira.dto.ChamadoResponseDTO;
import com.example.jira.enums.Prioridade;
import com.example.jira.enums.Status;
import com.example.jira.model.Categoria;
import com.example.jira.model.Chamado;
import com.example.jira.model.Comentario;
import com.example.jira.model.Subtopico;
import com.example.jira.repository.CategoriaRepository;
import com.example.jira.repository.ChamadoRepository;
import com.example.jira.repository.SubtopicoRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;
    private final CategoriaRepository categoriaRepository;
    private final SubtopicoRepository subtopicoRepository;

    public ChamadoService(ChamadoRepository chamadoRepository,CategoriaRepository categoriaRepository,SubtopicoRepository subtopicoRepository) {
        this.chamadoRepository = chamadoRepository;
        this.categoriaRepository= categoriaRepository;
        this.subtopicoRepository = subtopicoRepository;

    }

    @Transactional
    public ChamadoResponseDTO criarChamado(ChamadoRequestDTO req, Long userId) {
        Categoria categoria = categoriaRepository.findById(req.getCategoriaId())
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));

        Subtopico subtopico = null;
        if (req.getSubtopicoId() != null) {
            subtopico = subtopicoRepository.findById(req.getSubtopicoId())
                    .orElseThrow(() -> new EntityNotFoundException("Subtópico não encontrado"));
        }

        Chamado chamado = new Chamado(categoria, subtopico, req.getPrioridade(),
                Status.ABERTO, req.getTitulo(), req.getDescricao(), req.getEscopo());
        chamado.setUserId(userId);
        chamado.setOutroSubtopico(req.getOutroSubtopico());

        return ChamadoResponseDTO.from(chamadoRepository.save(chamado));
    }

    public Chamado buscarChamadoPorId(Integer id) {
        return chamadoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Chamado não encontrado"));
    }

    public Chamado fecharChamado(Integer id) {
        Chamado chamado = buscarChamadoPorId(id);
        chamado.fechar();
        return chamadoRepository.save(chamado);
    }

    public Chamado alterarStatusChamado(Integer id, Status novoStatus) {
        Chamado chamado = buscarChamadoPorId(id);
        chamado.alterarStatus(novoStatus);
        return chamadoRepository.save(chamado);
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
        return chamadoRepository.save(chamado);
    }

    public List<Chamado> listarChamados() {
        return chamadoRepository.findAll();
    }

    public List<Chamado> listarChamadoPorStatus(Status status) {
        return chamadoRepository.findByStatus(status);
    }

    public List<Chamado> listarChamadosPorPrioridade(Prioridade prioridade) {
        return chamadoRepository.findByPrioridade(prioridade);
    }
}