package com.example.jira.service;

import com.example.jira.dto.ChamadoRequestDTO;
import com.example.jira.dto.ChamadoResponseDTO;
import com.example.jira.enums.Escopo;
import com.example.jira.enums.Prioridade;
import com.example.jira.enums.Status;
import com.example.jira.model.*;
import com.example.jira.repository.CategoriaRepository;
import com.example.jira.repository.ChamadoRepository;
import com.example.jira.repository.PortalRepository;
import com.example.jira.repository.SubtopicoRepository;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChamadoServiceTest {

    @Mock
    private ChamadoRepository chamadoRepository;
    @Mock
    private CategoriaRepository categoriaRepository;
    @Mock
    private SubtopicoRepository subtopicoRepository;
    @Mock
    private PortalRepository portalRepository;

    @InjectMocks
    private ChamadoService chamadoService;

    private static final Long PORTAL_ID = 1L;
    private static final int CATEGORIA_ID = 1;
    private static final int SUBTOPICO_ID = 1;
    private static final int CHAMADO_ID = 1;
    private static final Long USER_ID = 1L;
    private static final String PORTAL_CODIGO = "PT";

    private Portal portal;
    private Categoria categoria;
    private Subtopico subtopico;
    private Chamado chamado;
    private ChamadoRequestDTO chamadoRequestDTO;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        portal = createPortal();
        categoria = createCategoria(portal);
        subtopico = createSubtopico(categoria);
        chamado = createChamado(portal, categoria, subtopico);
        chamadoRequestDTO = createChamadoRequestDTO();
    }

    private Portal createPortal() {
        Portal p = new Portal("Portal Teste", "Descrição Portal");
        p.setId(PORTAL_ID);
        p.setCodigo(PORTAL_CODIGO);
        return p;
    }

    private Categoria createCategoria(Portal portal) {
        Categoria c = new Categoria("Categoria Teste", portal);
        c.setId(CATEGORIA_ID);
        return c;
    }

    private Subtopico createSubtopico(Categoria categoria) {
        Subtopico s = new Subtopico("Subtópico Teste", categoria);
        s.setId(SUBTOPICO_ID);
        return s;
    }

    private Chamado createChamado(Portal portal, Categoria categoria, Subtopico subtopico) {
        Chamado ch = new Chamado();
        ch.setId(CHAMADO_ID);
        ch.setPortal(portal);
        ch.setCategoria(categoria);
        ch.setSubtopico(subtopico);
        ch.setPrioridade(Prioridade.ALTA);
        ch.setStatus(Status.ABERTO);
        ch.setTitulo("Título");
        ch.setDescricao("Descrição");
        ch.setEscopo(Escopo.TODOS);
        ch.setUserId(USER_ID);
        return ch;
    }

    private ChamadoRequestDTO createChamadoRequestDTO() {
        ChamadoRequestDTO dto = new ChamadoRequestDTO();
        dto.setPortalId(PORTAL_ID);
        dto.setCategoriaId(CATEGORIA_ID);
        dto.setSubtopicoId(SUBTOPICO_ID);
        dto.setPrioridade(Prioridade.ALTA);
        dto.setTitulo("Título");
        dto.setDescricao("Descrição");
        dto.setEscopo(Escopo.TODOS);
        return dto;
    }

    @Test
    @DisplayName("Deve criar um chamado com sucesso")
    void criarChamado_Success() {
        when(portalRepository.findById(PORTAL_ID)).thenReturn(Optional.of(portal));
        when(categoriaRepository.findById(CATEGORIA_ID)).thenReturn(Optional.of(categoria));
        when(subtopicoRepository.findById(SUBTOPICO_ID)).thenReturn(Optional.of(subtopico));

        // Capture the argument to mock the two-step save
        ArgumentCaptor<Chamado> chamadoCaptor = ArgumentCaptor.forClass(Chamado.class);
        when(chamadoRepository.save(chamadoCaptor.capture())).thenAnswer(invocation -> {
            Chamado savedChamado = invocation.getArgument(0);
            if (savedChamado.getId() == null) { // First save
                savedChamado.setId(CHAMADO_ID);
            }
            return savedChamado;
        });

        ChamadoResponseDTO response = chamadoService.criarChamado(chamadoRequestDTO, USER_ID);

        verify(portalRepository).findById(PORTAL_ID);
        verify(categoriaRepository).findById(CATEGORIA_ID);
        verify(subtopicoRepository).findById(SUBTOPICO_ID);
        verify(chamadoRepository, times(2)).save(any(Chamado.class));

        assertNotNull(response);
        assertEquals(PORTAL_CODIGO + "-" + CHAMADO_ID, chamadoCaptor.getValue().getCodigo());
        assertEquals("Título", response.titulo());
        assertEquals(Status.ABERTO, response.status());
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando o portal não for encontrado")
    void criarChamado_PortalNotFound() {
        when(portalRepository.findById(PORTAL_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            chamadoService.criarChamado(chamadoRequestDTO, USER_ID);
        });

        verify(portalRepository).findById(PORTAL_ID);
        verify(chamadoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando a categoria não pertence ao portal")
    void criarChamado_CategoriaDoesNotBelongToPortal() {
        Portal anotherPortal = new Portal("Outro Portal", "Desc");
        anotherPortal.setId(2L);
        categoria.setPortal(anotherPortal); // Categoria pertence a outro portal

        when(portalRepository.findById(PORTAL_ID)).thenReturn(Optional.of(portal));
        when(categoriaRepository.findById(CATEGORIA_ID)).thenReturn(Optional.of(categoria));

        assertThrows(IllegalArgumentException.class, () -> {
            chamadoService.criarChamado(chamadoRequestDTO, USER_ID);
        });
    }

    @Test
    @DisplayName("Deve buscar um chamado por ID com sucesso")
    void buscarChamadoPorId_Success() {
        when(chamadoRepository.findById(CHAMADO_ID)).thenReturn(Optional.of(chamado));

        Chamado result = chamadoService.buscarChamadoPorId(CHAMADO_ID);

        assertNotNull(result);
        assertEquals(chamado.getId(), result.getId());
        verify(chamadoRepository).findById(CHAMADO_ID);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao buscar chamado com ID inexistente")
    void buscarChamadoPorId_NotFound() {
        when(chamadoRepository.findById(CHAMADO_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            chamadoService.buscarChamadoPorId(CHAMADO_ID);
        });
    }

    @Test
    @DisplayName("Deve fechar um chamado com sucesso")
    void fecharChamado_Success() {
        when(chamadoRepository.findById(CHAMADO_ID)).thenReturn(Optional.of(chamado));
        when(chamadoRepository.save(any(Chamado.class))).thenReturn(chamado);

        chamadoService.fecharChamado(CHAMADO_ID);

        assertEquals(Status.FECHADO, chamado.getStatus());
        verify(chamadoRepository).save(chamado);
    }

    @Test
    @DisplayName("Deve adicionar um comentário a um chamado com sucesso")
    void adicionarComentario_Success() {
        when(chamadoRepository.findById(CHAMADO_ID)).thenReturn(Optional.of(chamado));
        when(chamadoRepository.save(any(Chamado.class))).thenReturn(chamado);

        chamadoService.adicionarComentario(CHAMADO_ID, "Novo comentário", USER_ID, "user");

        assertEquals(1, chamado.getComentarios().size());
        assertEquals("Novo comentário", chamado.getComentarios().get(0).getMensagem());
        verify(chamadoRepository).save(chamado);
    }

    @Test
    @DisplayName("Deve lançar IllegalStateException ao adicionar comentário em chamado fechado")
    void adicionarComentario_ChamadoFechado() {
        chamado.fechar();
        when(chamadoRepository.findById(CHAMADO_ID)).thenReturn(Optional.of(chamado));

        assertThrows(IllegalStateException.class, () -> {
            chamadoService.adicionarComentario(CHAMADO_ID, "Comentário inválido", USER_ID, "user");
        });

        verify(chamadoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar todos os chamados")
    void listarChamados_Success() {
        when(chamadoRepository.findAll()).thenReturn(Collections.singletonList(chamado));

        List<Chamado> chamados = chamadoService.listarChamados();

        assertFalse(chamados.isEmpty());
        assertEquals(1, chamados.size());
        verify(chamadoRepository).findAll();
    }

    @Test
    @DisplayName("Deve listar chamados por status")
    void listarChamadoPorStatus_Success() {
        when(chamadoRepository.findByStatus(Status.ABERTO)).thenReturn(Collections.singletonList(chamado));

        List<Chamado> chamados = chamadoService.listarChamadoPorStatus(Status.ABERTO);

        assertFalse(chamados.isEmpty());
        assertEquals(1, chamados.size());
        assertEquals(Status.ABERTO, chamados.get(0).getStatus());
        verify(chamadoRepository).findByStatus(Status.ABERTO);
    }

    @Test
    @DisplayName("Deve listar chamados por prioridade")
    void listarChamadosPorPrioridade_Success() {
        when(chamadoRepository.findByPrioridade(Prioridade.ALTA)).thenReturn(Collections.singletonList(chamado));

        List<Chamado> chamados = chamadoService.listarChamadosPorPrioridade(Prioridade.ALTA);

        assertFalse(chamados.isEmpty());
        assertEquals(1, chamados.size());
        assertEquals(Prioridade.ALTA, chamados.get(0).getPrioridade());
        verify(chamadoRepository).findByPrioridade(Prioridade.ALTA);
    }

    @Test
    @DisplayName("Deve criar um chamado com sucesso sem subtopico")
    void criarChamado_Success_NoSubtopico() {
        chamadoRequestDTO.setSubtopicoId(null);

        when(portalRepository.findById(PORTAL_ID)).thenReturn(Optional.of(portal));
        when(categoriaRepository.findById(CATEGORIA_ID)).thenReturn(Optional.of(categoria));

        ArgumentCaptor<Chamado> chamadoCaptor = ArgumentCaptor.forClass(Chamado.class);
        when(chamadoRepository.save(chamadoCaptor.capture())).thenAnswer(invocation -> {
            Chamado savedChamado = invocation.getArgument(0);
            if (savedChamado.getId() == null) {
                savedChamado.setId(CHAMADO_ID);
            }
            return savedChamado;
        });

        ChamadoResponseDTO response = chamadoService.criarChamado(chamadoRequestDTO, USER_ID);

        verify(portalRepository).findById(PORTAL_ID);
        verify(categoriaRepository).findById(CATEGORIA_ID);
        verify(subtopicoRepository, never()).findById(any());
        verify(chamadoRepository, times(2)).save(any(Chamado.class));

        assertNotNull(response);
        assertEquals(PORTAL_CODIGO + "-" + CHAMADO_ID, chamadoCaptor.getValue().getCodigo());
        assertNull(chamadoCaptor.getValue().getSubtopico());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando o subtópico não pertence à categoria")
    void criarChamado_SubtopicoDoesNotBelongToCategoria() {
        Categoria anotherCategoria = new Categoria("Outra Categoria", portal);
        anotherCategoria.setId(2);
        subtopico.setCategoria(anotherCategoria); // Subtópico pertence a outra categoria

        when(portalRepository.findById(PORTAL_ID)).thenReturn(Optional.of(portal));
        when(categoriaRepository.findById(CATEGORIA_ID)).thenReturn(Optional.of(categoria));
        when(subtopicoRepository.findById(SUBTOPICO_ID)).thenReturn(Optional.of(subtopico));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            chamadoService.criarChamado(chamadoRequestDTO, USER_ID);
        });

        assertEquals("O subtópico não pertence à categoria informada.", exception.getMessage());
    }
}