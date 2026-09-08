package com.example.jira.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.example.jira.model.Time;
import com.example.jira.repository.TimeRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class TimeService {

    private final TimeRepository timeRepository;

    public TimeService(TimeRepository timeRepository) {
        this.timeRepository = timeRepository;
    }

    public List<Time> listarTimesDoUsuario(Long userId) {
        return timeRepository.findByMembrosContaining(userId);
    }

    public Set<Time> buscarTimesDoUsuario(Set<Integer> timeIds, Long userId) {
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

    public Set<Time> obterTimesParaEscopoSomenteEu(Long userId) {
        return new HashSet<>(timeRepository.findByMembrosContaining(userId));
    }
}