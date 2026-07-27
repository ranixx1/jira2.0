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
import com.example.jira.model.Portal;
import com.example.jira.model.Subtopico;
import com.example.jira.repository.CategoriaRepository;
import com.example.jira.repository.ChamadoRepository;
import com.example.jira.repository.PortalRepository;
import com.example.jira.repository.SubtopicoRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;
    private final CategoriaRepository categoriaRepository;
    private final SubtopicoRepository subtopicoRepository;
    private final PortalRepository portalRepository;

    public ChamadoService(
            ChamadoRepository chamadoRepository,
            CategoriaRepository categoriaRepository,
            SubtopicoRepository subtopicoRepository,
            PortalRepository portalRepository) {

        this.chamadoRepository = chamadoRepository;
        this.categoriaRepository = categoriaRepository;
        this.subtopicoRepository = subtopicoRepository;
        this.portalRepository = portalRepository;
    }

    @Transactional
    public ChamadoResponseDTO criarChamado(ChamadoRequestDTO req, Long userId) {

        Portal portal = buscarPortal(req.getPortalId());

        Categoria categoria = buscarCategoria(req.getCategoriaId());

        validarCategoriaDoPortal(portal, categoria);

        Subtopico subtopico = buscarSubtopico(req.getSubtopicoId());

        if (subtopico != null) {
            validarSubtopicoDaCategoria(subtopico, categoria);
        }

        Chamado chamado = new Chamado(
                portal,
                categoria,
                subtopico,
                req.getPrioridade(),
                Status.ABERTO,
                req.getTitulo(),
                req.getDescricao(),
                req.getEscopo());

        chamado.setUserId(userId);
        chamado.setOutroSubtopico(req.getOutroSubtopico());

        chamado = chamadoRepository.save(chamado);

        chamado.setCodigo(
                portal.getCodigo() + "-" + chamado.getId());

        chamado = chamadoRepository.save(chamado);

        return ChamadoResponseDTO.from(chamado);
    }

    public Chamado buscarChamadoPorId(Integer id) {
        return chamadoRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Chamado não encontrado"));
    }

    @Transactional
    public Chamado fecharChamado(Integer id) {

        Chamado chamado = buscarChamadoPorId(id);

        chamado.fechar();

        return chamadoRepository.save(chamado);
    }

    @Transactional
    public Chamado alterarStatusChamado(Integer id, Status novoStatus) {

        Chamado chamado = buscarChamadoPorId(id);

        chamado.alterarStatus(novoStatus);

        return chamadoRepository.save(chamado);
    }

    @Transactional
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
    private Portal buscarPortal(Long portalId) {

        return portalRepository.findById(portalId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Portal não encontrado"));
    }

    private Categoria buscarCategoria(Integer categoriaId) {

        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Categoria não encontrada"));
    }

    private Subtopico buscarSubtopico(Integer subtopicoId) {

        if (subtopicoId == null) {
            return null;
        }

        return subtopicoRepository.findById(subtopicoId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Subtópico não encontrado"));
    }

    private void validarCategoriaDoPortal(
            Portal portal,
            Categoria categoria) {

        if (!categoria.getPortal().getId().equals(portal.getId())) {

            throw new IllegalArgumentException(
                    "A categoria não pertence ao portal informado.");
        }
    }

    private void validarSubtopicoDaCategoria(
            Subtopico subtopico,
            Categoria categoria) {

        if (!subtopico.getCategoria().getId().equals(categoria.getId())) {

            throw new IllegalArgumentException(
                    "O subtópico não pertence à categoria informada.");
        }
    }
}