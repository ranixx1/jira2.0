package com.example.jira.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.jira.model.ChamadoHistorico;

public interface ChamadoHistoricoRepository
        extends JpaRepository<ChamadoHistorico, Integer> {

    List<ChamadoHistorico> findByChamadoIdOrderByDataHoraAsc(Integer chamadoId);
}