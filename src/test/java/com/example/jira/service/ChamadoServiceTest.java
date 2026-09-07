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
import com.example.jira.repository.ChamadoHistoricoRepository;
import com.example.jira.repository.TimeRepository;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

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
    @Mock
    private ChamadoHistoricoRepository chamadoHistoricoRepository;
    @Mock
    private TimeRepository timeRepository;
    @Mock
    private ChamadoAuthorizationService authorizationService;

    @InjectMocks
    private ChamadoService chamadoService;

    private static final Long PORTAL_ID = 1L;
    private static final int CATEGORIA_ID = 1;
    private static final int SUBTOPICO_ID = 1;
    private static final int CHAMADO_ID = 1;
    private static final Long USER_ID = 1L;
    private static final Long OUTRO_USER_ID = 2L;
    private static final String PORTAL_CODIGO = "PT";
    private static final String USERNAME = "testUser";
    private static final String ROLE = "ROLE_USER";

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
        chamado = createChamado(CHAMADO_ID, portal, categoria, subtopico, Escopo.TODOS, USER_ID);
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

    private Chamado createChamado(
            int id, Portal portal, Categoria categoria, Subtopico subtopico,
            Escopo escopo, Long userId) {

        Chamado ch = new Chamado();
        ch.setId(id);
        ch.setPortal(portal);
        ch.setCategoria(categoria);
        ch.setSubtopico(subtopico);
        ch.setPrioridade(Prioridade.ALTA);
        ch.setStatus(Status.ABERTO);
        ch.setTitulo("Título");
        ch.setDescricao("Descrição");
        ch.setEscopo(escopo);
        ch.setUserId(userId);
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

        ArgumentCaptor<Chamado> chamadoCaptor = ArgumentCaptor.forClass(Chamado.class);
        when(chamadoRepository.save(chamadoCaptor.capture())).thenAnswer(invocation -> {
            Chamado savedChamado = invocation.getArgument(0);
            if (savedChamado.getId() == null) {
                savedChamado.setId(CHAMADO_ID);
            }
            return savedChamado;
        });

        ChamadoResponseDTO response = chamadoService.criarChamado(chamadoRequestDTO, USER_ID, USERNAME);

        verify(portalRepository).findById(PORTAL_ID);
        verify(categoriaRepository).findById(CATEGORIA_ID);
        verify(subtopicoRepository).findById(SUBTOPICO_ID);
        verify(chamadoRepository, times(2)).save(any(Chamado.class));
        verify(chamadoHistoricoRepository).save(any(ChamadoHistorico.class));

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
            chamadoService.criarChamado(chamadoRequestDTO, USER_ID, USERNAME);
        });

        verify(portalRepository).findById(PORTAL_ID);
        verify(chamadoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando a categoria não pertence ao portal")
    void criarChamado_CategoriaDoesNotBelongToPortal() {
        Portal anotherPortal = new Portal("Outro Portal", "Desc");
        anotherPortal.setId(2L);
        categoria.setPortal(anotherPortal);

        when(portalRepository.findById(PORTAL_ID)).thenReturn(Optional.of(portal));
        when(categoriaRepository.findById(CATEGORIA_ID)).thenReturn(Optional.of(categoria));

        assertThrows(IllegalArgumentException.class, () -> {
            chamadoService.criarChamado(chamadoRequestDTO, USER_ID, USERNAME);
        });
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

        ChamadoResponseDTO response = chamadoService.criarChamado(chamadoRequestDTO, USER_ID, USERNAME);

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
        subtopico.setCategoria(anotherCategoria);

        when(portalRepository.findById(PORTAL_ID)).thenReturn(Optional.of(portal));
        when(categoriaRepository.findById(CATEGORIA_ID)).thenReturn(Optional.of(categoria));
        when(subtopicoRepository.findById(SUBTOPICO_ID)).thenReturn(Optional.of(subtopico));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            chamadoService.criarChamado(chamadoRequestDTO, USER_ID, USERNAME);
        });

        assertEquals("O subtópico não pertence à categoria informada.", exception.getMessage());
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
        when(authorizationService.podeFechar(chamado, ROLE)).thenReturn(true);
        when(chamadoRepository.save(any(Chamado.class))).thenReturn(chamado);

        chamadoService.fecharChamado(CHAMADO_ID, USER_ID, USERNAME, ROLE);

        assertEquals(Status.FECHADO, chamado.getStatus());
        verify(chamadoRepository).save(chamado);
        verify(chamadoHistoricoRepository).save(any(ChamadoHistorico.class));
    }

    @Test
    @DisplayName("Deve adicionar um comentário a um chamado com sucesso")
    void adicionarComentario_Success() {
        when(chamadoRepository.findById(CHAMADO_ID)).thenReturn(Optional.of(chamado));
        when(authorizationService.podeComentar(chamado, ROLE, USER_ID)).thenReturn(true);
        when(chamadoRepository.save(any(Chamado.class))).thenReturn(chamado);

        chamadoService.adicionarComentario(CHAMADO_ID, "Novo comentário", USER_ID, USERNAME, ROLE);

        assertEquals(1, chamado.getComentarios().size());
        assertEquals("Novo comentário", chamado.getComentarios().get(0).getMensagem());
        verify(chamadoRepository).save(chamado);
        verify(chamadoHistoricoRepository).save(any(ChamadoHistorico.class));
    }

    @Test
    @DisplayName("Deve lançar AccessDeniedException ao adicionar comentário sem permissão")
    void adicionarComentario_SemPermissao() {
        when(chamadoRepository.findById(CHAMADO_ID)).thenReturn(Optional.of(chamado));
        when(authorizationService.podeComentar(chamado, ROLE, USER_ID)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> {
            chamadoService.adicionarComentario(CHAMADO_ID, "Novo comentário", USER_ID, USERNAME, ROLE);
        });

        verify(chamadoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar IllegalStateException ao adicionar comentário em chamado fechado")
    void adicionarComentario_ChamadoFechado() {
        chamado.fechar();
        when(chamadoRepository.findById(CHAMADO_ID)).thenReturn(Optional.of(chamado));
        when(authorizationService.podeComentar(chamado, ROLE, USER_ID)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> {
            chamadoService.adicionarComentario(CHAMADO_ID, "Comentário inválido", USER_ID, USERNAME, ROLE);
        });

        verify(chamadoRepository, never()).save(any());
    }

    @Nested
    @DisplayName("Visibilidade por Escopo")
    class VisibilidadePorEscopo {

        @Test
        @DisplayName("listarChamados esconde chamados de outros usuários caso não haja permissão")
        void listarChamados_FiltraPorEscopo() {
            Chamado meu = createChamado(1, portal, categoria, subtopico, Escopo.SOMENTE_EU, USER_ID);
            Chamado deOutro = createChamado(2, portal, categoria, subtopico, Escopo.SOMENTE_EU, OUTRO_USER_ID);
            Chamado publico = createChamado(3, portal, categoria, subtopico, Escopo.TODOS, OUTRO_USER_ID);

            when(chamadoRepository.findAllComDetalhes())
                    .thenReturn(List.of(meu, deOutro, publico));
            
            when(authorizationService.podeVisualizar(meu, ROLE, USER_ID)).thenReturn(true);
            when(authorizationService.podeVisualizar(deOutro, ROLE, USER_ID)).thenReturn(false);
            when(authorizationService.podeVisualizar(publico, ROLE, USER_ID)).thenReturn(true);

            List<Chamado> resultado = chamadoService.listarChamados(USER_ID, ROLE);

            assertEquals(2, resultado.size());
            assertTrue(resultado.contains(meu));
            assertTrue(resultado.contains(publico));
            assertFalse(resultado.contains(deOutro));
            verify(chamadoRepository).findAllComDetalhes();
        }

        @Test
        @DisplayName("listarChamadoPorStatus usa a query otimizada e respeita o escopo")
        void listarChamadoPorStatus_FiltraPorEscopoEUsaQueryComDetalhes() {
            Chamado deOutro = createChamado(2, portal, categoria, subtopico, Escopo.SOMENTE_EU, OUTRO_USER_ID);

            when(chamadoRepository.findByStatusComDetalhes(Status.ABERTO))
                    .thenReturn(List.of(deOutro));
            
            when(authorizationService.podeVisualizar(deOutro, ROLE, USER_ID)).thenReturn(false);

            List<Chamado> resultado = chamadoService.listarChamadoPorStatus(Status.ABERTO, USER_ID, ROLE);

            assertTrue(resultado.isEmpty());
            verify(chamadoRepository).findByStatusComDetalhes(Status.ABERTO);
        }

        @Test
        @DisplayName("listarChamadosPorPrioridade respeita o escopo")
        void listarChamadosPorPrioridade_FiltraPorEscopo() {
            Chamado meu = createChamado(1, portal, categoria, subtopico, Escopo.SOMENTE_EU, USER_ID);
            Chamado deOutro = createChamado(2, portal, categoria, subtopico, Escopo.SOMENTE_EU, OUTRO_USER_ID);

            when(chamadoRepository.findByPrioridade(Prioridade.ALTA))
                    .thenReturn(List.of(meu, deOutro));
            
            when(authorizationService.podeVisualizar(meu, ROLE, USER_ID)).thenReturn(true);
            when(authorizationService.podeVisualizar(deOutro, ROLE, USER_ID)).thenReturn(false);

            List<Chamado> resultado = chamadoService.listarChamadosPorPrioridade(Prioridade.ALTA, USER_ID, ROLE);

            assertEquals(1, resultado.size());
            assertEquals(meu.getId(), resultado.get(0).getId());
        }

        @Test
        @DisplayName("buscarChamadoVisivel retorna o chamado quando autorizado")
        void buscarChamadoVisivel_Autorizado_Sucesso() {
            chamado.setEscopo(Escopo.TODOS);
            when(chamadoRepository.findById(CHAMADO_ID)).thenReturn(Optional.of(chamado));
            when(authorizationService.podeVisualizar(chamado, ROLE, OUTRO_USER_ID)).thenReturn(true);

            Chamado resultado = chamadoService.buscarChamadoVisivel(CHAMADO_ID, OUTRO_USER_ID, ROLE);

            assertEquals(chamado.getId(), resultado.getId());
        }

        @Test
        @DisplayName("buscarChamadoVisivel lança EntityNotFoundException quando não autorizado")
        void buscarChamadoVisivel_NaoAutorizado_LancaEntityNotFoundException() {
            chamado.setEscopo(Escopo.SOMENTE_EU);
            when(chamadoRepository.findById(CHAMADO_ID)).thenReturn(Optional.of(chamado));
            when(authorizationService.podeVisualizar(chamado, ROLE, OUTRO_USER_ID)).thenReturn(false);

            assertThrows(EntityNotFoundException.class, () ->
                    chamadoService.buscarChamadoVisivel(CHAMADO_ID, OUTRO_USER_ID, ROLE));
        }
    }
}