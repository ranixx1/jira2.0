package com.example.jira.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.jira.dto.ChamadoHistoricoDTO;
import com.example.jira.dto.ChamadoRequestDTO;
import com.example.jira.dto.ChamadoResponseDTO;
import com.example.jira.enums.Escopo;
import com.example.jira.model.ChamadoHistorico;
import com.example.jira.repository.ChamadoHistoricoRepository;
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
    private final ChamadoHistoricoRepository chamadoHistoricoRepository;

    public ChamadoService(
            ChamadoRepository chamadoRepository,
            CategoriaRepository categoriaRepository,
            SubtopicoRepository subtopicoRepository,
            PortalRepository portalRepository,
            ChamadoHistoricoRepository chamadoHistoricoRepository) {

        this.chamadoRepository = chamadoRepository;
        this.categoriaRepository = categoriaRepository;
        this.subtopicoRepository = subtopicoRepository;
        this.portalRepository = portalRepository;
        this.chamadoHistoricoRepository = chamadoHistoricoRepository;
    }

    @Transactional
    public ChamadoResponseDTO criarChamado(
            ChamadoRequestDTO req,
            Long userId,
            String username) {

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

        ChamadoHistorico historico = new ChamadoHistorico(
                chamado,
                "CRIADO",
                "Chamado criado",
                userId,
                username);

        chamadoHistoricoRepository.save(historico);

        return ChamadoResponseDTO.from(chamado);
    }

    public Chamado buscarChamadoPorId(Integer id) {
        return chamadoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Chamado não encontrado"));
    }

    @Transactional
    public Chamado fecharChamado(Integer id) {

        Chamado chamado = buscarChamadoPorId(id);

        chamado.fechar();

        return chamadoRepository.save(chamado);
    }

    @Transactional
    public Chamado alterarStatus(
            Integer id,
            Status novoStatus,
            Long userId,
            String username) {

        Chamado chamado = buscarChamadoPorId(id);

        if (chamado.getStatus() == Status.FECHADO) {
            throw new IllegalStateException(
                    "Não é possível alterar o status de um chamado fechado.");
        }

        Status statusAnterior = chamado.getStatus();

        if (statusAnterior == novoStatus) {
            return chamado;
        }

        chamado.alterarStatus(novoStatus);
        chamado.setAtualizadoPorUserId(userId);
        chamado.setHorario_atualizacao(LocalDateTime.now());

        chamado = chamadoRepository.save(chamado);

        ChamadoHistorico historico = new ChamadoHistorico(
                chamado,
                "STATUS_ALTERADO",
                "Status alterado de "
                        + statusAnterior
                        + " para "
                        + novoStatus,
                userId,
                username);

        chamadoHistoricoRepository.save(historico);

        return chamado;
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

        chamado = chamadoRepository.save(chamado);

        ChamadoHistorico historico = new ChamadoHistorico(
                chamado,
                "COMENTARIO_ADICIONADO",
                "Comentário adicionado",
                userId,
                username);

        chamadoHistoricoRepository.save(historico);

        return chamado;
    }

    public List<Chamado> listarChamados(Long userId) {
        return chamadoRepository.findAllComDetalhes().stream()
                .filter(c -> podeVisualizar(c, userId))
                .toList();
    }

    public List<Chamado> listarChamadoPorStatus(Status status, Long userId) {
        return chamadoRepository.findByStatusComDetalhes(status).stream()
                .filter(c -> podeVisualizar(c, userId))
                .toList();
    }

    public List<Chamado> listarChamadosPorPrioridade(Prioridade prioridade, Long userId) {
        return chamadoRepository.findByPrioridade(prioridade).stream()
                .filter(c -> podeVisualizar(c, userId))
                .toList();
    }

    public List<Chamado> listarChamadosPorUsuario(Long userId) {
        return chamadoRepository.findByUserId(userId);
    }

    public Chamado buscarChamadoVisivel(Integer id, Long userId) {
        Chamado chamado = buscarChamadoPorId(id);

        if (!podeVisualizar(chamado, userId)) {
            // 404 em vez de 403: não confirma pro solicitante que o chamado existe
            throw new EntityNotFoundException("Chamado não encontrado");
        }

        return chamado;
    }

    private boolean podeVisualizar(Chamado chamado, Long userId) {
        return chamado.getEscopo() == Escopo.TODOS
                || chamado.getUserId().equals(userId);
    }

    private Portal buscarPortal(Long portalId) {

        return portalRepository.findById(portalId)
                .orElseThrow(() -> new EntityNotFoundException("Portal não encontrado"));
    }

    private Categoria buscarCategoria(Integer categoriaId) {

        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));
    }

    private Subtopico buscarSubtopico(Integer subtopicoId) {

        if (subtopicoId == null) {
            return null;
        }

        return subtopicoRepository.findById(subtopicoId)
                .orElseThrow(() -> new EntityNotFoundException("Subtópico não encontrado"));
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
    public List<ChamadoHistoricoDTO> listarHistorico(Integer chamadoId) {

    buscarChamadoPorId(chamadoId);

    return chamadoHistoricoRepository
            .findByChamadoIdOrderByDataHoraAsc(chamadoId)
            .stream()
            .map(ChamadoHistoricoDTO::from)
            .toList();
}
}