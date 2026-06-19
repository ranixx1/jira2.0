package com.example.jira.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.jira.enums.*;
import com.example.jira.model.Chamado;

import java.util.List;

@Repository
public interface ChamadoRepository extends JpaRepository<Chamado, Integer> {
    List<Chamado> findByTipo(Tipo tipo);

    List<Chamado> findByStatus(Status status);

    List<Chamado> findByEscopo(Escopo escopo);

    List<Chamado> findByStatusAndEscopo(Status status, Escopo escopo);

    List<Chamado> findByPrioridade(Prioridade prioridade);

    List<Chamado> findByUsuarioId(Integer usuarioId);

}
