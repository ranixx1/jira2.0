package com.example.jira.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.example.jira.dto.ChamadoHistoricoDTO;
import com.example.jira.dto.ChamadoRequestDTO;
import com.example.jira.dto.ChamadoResponseDTO;
import com.example.jira.enums.Escopo;
import com.example.jira.enums.Prioridade;
import com.example.jira.enums.Status;
import com.example.jira.model.Categoria;
import com.example.jira.model.Chamado;
import com.example.jira.model.ChamadoHistorico;
import com.example.jira.model.Comentario;
import com.example.jira.model.Portal;
import com.example.jira.model.Subtopico;
import com.example.jira.model.Time;
import com.example.jira.repository.CategoriaRepository;
import com.example.jira.repository.ChamadoHistoricoRepository;
import com.example.jira.repository.ChamadoRepository;
import com.example.jira.repository.PortalRepository;
import com.example.jira.repository.SubtopicoRepository;
import com.example.jira.repository.TimeRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;
    private final CategoriaRepository categoriaRepository;
    private final SubtopicoRepository subtopicoRepository;
    private final PortalRepository portalRepository;
    private final ChamadoHistoricoRepository chamadoHistoricoRepository;
    private final TimeRepository timeRepository;
    private final ChamadoAuthorizationService authorizationService;

    public ChamadoService(
            ChamadoRepository chamadoRepository,
            CategoriaRepository categoriaRepository,
            SubtopicoRepository subtopicoRepository,
            PortalRepository portalRepository,
            ChamadoHistoricoRepository chamadoHistoricoRepository,
            TimeRepository timeRepository,
            ChamadoAuthorizationService authorizationService) {

        this.chamadoRepository = chamadoRepository;
        this.categoriaRepository = categoriaRepository;
        this.subtopicoRepository = subtopicoRepository;
        this.portalRepository = portalRepository;
        this.chamadoHistoricoRepository = chamadoHistoricoRepository;
        this.timeRepository = timeRepository;
        this.authorizationService = authorizationService;
    }

    @Transactional
    public ChamadoResponseDTO criarChamado(ChamadoRequestDTO req, Long userId, String username) {
        Portal portal = buscarPortal(req.getPortalId());
        Categoria categoria = buscarCategoria(req.getCategoriaId());

        validarCategoriaDoPortal(portal, categoria);

        Subtopico subtopico = buscarSubtopico(req.getSubtopicoId());

        if (subtopico != null) {
            validarSubtopicoDaCategoria(subtopico, categoria);
        }

	Set<Time> times;

	if (req.getEscopo() == Escopo.SOMENTE_EU) {
	    times = new HashSet<>(timeRepository.findByMembrosContaining(userId));
	} else {
	    times = new HashSet<>();
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
        chamado.setTimes(times);

        chamado = chamadoRepository.save(chamado);

        chamado.setCodigo(portal.getCodigo() + "-" + chamado.getId());

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

    public Chamado buscarChamadoVisivel(Integer id, Long userId, String role) {
        Chamado chamado = buscarChamadoPorId(id);

        if (!authorizationService.podeVisualizar(chamado, role, userId)) {
            throw new EntityNotFoundException("Chamado não encontrado");
        }

        return chamado;
    }

    @Transactional
    public Chamado alterarStatus(Integer id, Status novoStatus, Long userId, String username, String role) {
        Chamado chamado = buscarChamadoPorId(id);

        if (!authorizationService.podeAlterarStatus(chamado, role)) {
            throw new AccessDeniedException("Usuário não possui permissão para alterar o status.");
        }

        if (chamado.getStatus() == Status.FECHADO) {
            throw new IllegalStateException("Não é possível alterar o status de um chamado fechado.");
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
                "Status alterado de " + statusAnterior + " para " + novoStatus,
                userId,
                username);

        chamadoHistoricoRepository.save(historico);

        return chamado;
    }

    @Transactional
    public Chamado fecharChamado(Integer id, Long userId, String username, String role) {
        Chamado chamado = buscarChamadoPorId(id);

        if (!authorizationService.podeFechar(chamado, role)) {
            throw new AccessDeniedException("Usuário não possui permissão para fechar o chamado.");
        }

        if (chamado.getStatus() == Status.FECHADO) {
            return chamado;
        }

        chamado.fechar();
        chamado.setAtualizadoPorUserId(userId);

        chamado = chamadoRepository.save(chamado);

        ChamadoHistorico historico = new ChamadoHistorico(
                chamado,
                "CHAMADO_FECHADO",
                "Chamado fechado",
                userId,
                username);

        chamadoHistoricoRepository.save(historico);

        return chamado;
    }

    @Transactional
    public Chamado adicionarComentario(Integer chamadoId, String mensagem, Long userId, String username, String role) {
        Chamado chamado = buscarChamadoPorId(chamadoId);

        if (!authorizationService.podeComentar(chamado, role, userId)) {
            throw new AccessDeniedException("Usuário não possui permissão para comentar neste chamado.");
        }

        if (chamado.getStatus() == Status.FECHADO) {
            throw new IllegalStateException("Não é possível adicionar comentários a um chamado fechado.");
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

    public List<Chamado> listarChamados(Long userId, String role) {
        return chamadoRepository.findAllComDetalhes()
                .stream()
                .filter(c -> authorizationService.podeVisualizar(c, role, userId))
                .toList();
    }

    public List<Chamado> listarChamadoPorStatus(Status status, Long userId, String role) {
        return chamadoRepository.findByStatusComDetalhes(status)
                .stream()
                .filter(c -> authorizationService.podeVisualizar(c, role, userId))
                .toList();
    }

    public List<Chamado> listarChamadosPorPrioridade(Prioridade prioridade, Long userId, String role) {
        return chamadoRepository.findByPrioridade(prioridade)
                .stream()
                .filter(c -> authorizationService.podeVisualizar(c, role, userId))
                .toList();
    }

    public List<Chamado> listarChamadosPorUsuario(Long userId) {
        return chamadoRepository.findByUserId(userId);
    }

    public List<ChamadoHistoricoDTO> listarHistorico(Integer chamadoId) {
        buscarChamadoPorId(chamadoId);

        return chamadoHistoricoRepository.findByChamadoIdOrderByDataHoraAsc(chamadoId)
                .stream()
                .map(ChamadoHistoricoDTO::from)
                .toList();
    }

    private Set<Time> buscarTimesDoUsuario(Set<Integer> timeIds, Long userId) {
        Set<Time> timesDoUsuario = new HashSet<>(timeRepository.findByMembrosContaining(userId));

        if (timeIds == null || timeIds.isEmpty()) {
            return new HashSet<>();
        }

        Set<Time> timesSelecionados = new HashSet<>(timeRepository.findAllById(timeIds));

        if (timesSelecionados.size() != timeIds.size()) {
            throw new EntityNotFoundException("Um ou mais times não foram encontrados.");
        }

        boolean possuiTimeNaoPertencente = !timesDoUsuario.containsAll(timesSelecionados);

        if (possuiTimeNaoPertencente) {
            throw new AccessDeniedException("Você só pode selecionar times dos quais faz parte.");
        }

        return timesSelecionados;
    }

    public List<Time> listarTimesDoUsuario(Long userId) {
        return timeRepository.findByMembrosContaining(userId);
    }

    private void validarEscopo(Escopo escopo, Set<Time> times) {
        if (escopo == Escopo.SOMENTE_EU && times.isEmpty()) {
            throw new IllegalArgumentException("Chamados com escopo SOMENTE_EU precisam possuir pelo menos um time.");
        }
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

    private void validarCategoriaDoPortal(Portal portal, Categoria categoria) {
        if (!categoria.getPortal().getId().equals(portal.getId())) {
            throw new IllegalArgumentException("A categoria não pertence ao portal informado.");
        }
    }

    private void validarSubtopicoDaCategoria(Subtopico subtopico, Categoria categoria) {
        if (!subtopico.getCategoria().getId().equals(categoria.getId())) {
            throw new IllegalArgumentException("O subtópico não pertence à categoria informada.");
        }
    }
}
