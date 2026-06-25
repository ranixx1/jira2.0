package com.example.jira.service;

import com.example.jira.model.Categoria;
import com.example.jira.model.Subtopico;
import com.example.jira.repository.CategoriaRepository;
import com.example.jira.repository.SubtopicoRepository;
import com.example.jira.repository.TimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChamadoConfigService {

    private final CategoriaRepository categoriaRepository;
    private final SubtopicoRepository subtopicoRepository;
    private final TimeRepository timeRepository;

    public List<Categoria> listarDisponiveis() {
        return categoriaRepository.findAll();
    }

    @Transactional
    public Categoria criarCategoria(String nome, Integer timeId) {
        return timeRepository.findById(timeId).map(time -> {
            Categoria novaCategoria = new Categoria(nome, time);
            return categoriaRepository.save(novaCategoria);
        }).orElseThrow(() -> new IllegalArgumentException("Time não encontrado"));
    }

    @Transactional
    public Subtopico criarSubtopico(String nome, Integer categoriaId) {
        return categoriaRepository.findById(categoriaId).map(categoria -> {
            Subtopico novoSubtopico = new Subtopico(nome, categoria);
            return subtopicoRepository.save(novoSubtopico);
        }).orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada"));
    }

    @Transactional
    public void deletarCategoria(Integer id) {
        if (!categoriaRepository.existsById(id)) {
            throw new IllegalArgumentException("Categoria não encontrada");
        }
        categoriaRepository.deleteById(id);
    }

    @Transactional
    public void deletarSubtopico(Integer id) {
        if (!subtopicoRepository.existsById(id)) {
            throw new IllegalArgumentException("Subtópico não encontrado");
        }
        subtopicoRepository.deleteById(id);
    }
}