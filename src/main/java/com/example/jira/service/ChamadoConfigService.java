package com.example.jira.service;

import com.example.jira.exception.ResourceAlreadyExistsException;
import com.example.jira.exception.ResourceNotFoundException;
import com.example.jira.model.Categoria;
import com.example.jira.model.Portal;
import com.example.jira.model.Subtopico;
import com.example.jira.repository.CategoriaRepository;
import com.example.jira.repository.PortalRepository;
import com.example.jira.repository.SubtopicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChamadoConfigService {

    private final CategoriaRepository categoriaRepository;
    private final SubtopicoRepository subtopicoRepository;
    private final PortalRepository portalRepository;

    public List<Portal> listarPortaisDisponiveis() {
        return portalRepository.findAll();
    }

    public List<Categoria> listarCategorias() {
        return categoriaRepository.findAll();
    }

    @Transactional
    public Portal criarPortal(String nome, String descricao) {

        if (portalRepository.existsByNome(nome)) {
            throw new ResourceAlreadyExistsException("Já existe um portal com o nome: " + nome);
        }

        Portal portal = new Portal(nome, descricao);

        return portalRepository.save(portal);
    }

    @Transactional
    public Categoria criarCategoria(String nome, Long portalId) {

        Portal portal = portalRepository.findById(portalId)
                .orElseThrow(() -> new ResourceNotFoundException("Portal não encontrado com o ID: " + portalId));

        if (categoriaRepository.existsByPortalIdAndNome(portalId, nome)) {
            throw new ResourceAlreadyExistsException("Já existe uma categoria com o nome '" + nome + "' neste portal.");
        }

        Categoria categoria = new Categoria(nome, portal);

        return categoriaRepository.save(categoria);
    }

    @Transactional
    public Subtopico criarSubtopico(String nome, Integer categoriaId) {

        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com o ID: " + categoriaId));

        if (subtopicoRepository.existsByCategoriaIdAndNome(categoriaId, nome)) {
            throw new ResourceAlreadyExistsException("Já existe um subtópico com o nome '" + nome + "' nesta categoria.");
        }

        return subtopicoRepository.save(new Subtopico(nome, categoria));
    }

    @Transactional
    public void deletarPortal(Long id) {

        if (!portalRepository.existsById(id)) {
            throw new ResourceNotFoundException("Portal não encontrado com o ID: " + id);
        }

        portalRepository.deleteById(id);
    }

    @Transactional
    public void deletarCategoria(Integer id) {
        if (!categoriaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Categoria não encontrada com o ID: " + id);
        }
        categoriaRepository.deleteById(id);
    }

    @Transactional
    public void deletarSubtopico(Integer id) {
        if (!subtopicoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Subtópico não encontrado com o ID: " + id);
        }
        subtopicoRepository.deleteById(id);
    }
}