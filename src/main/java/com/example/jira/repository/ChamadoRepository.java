package com.example.jira.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.jira.enums.*;
import com.example.jira.model.Chamado;

import java.util.List;

@Repository
public interface ChamadoRepository extends JpaRepository<Chamado, Integer> {

       @Query("SELECT c FROM Chamado c " +
                     "LEFT JOIN FETCH c.portal " +
                     "LEFT JOIN FETCH c.categoria cat " +
                     "LEFT JOIN FETCH c.subtopico " +
                     "LEFT JOIN FETCH c.comentarios " +
                     "LEFT JOIN FETCH cat.time")
       List<Chamado> findAllComDetalhes();

       @Query("SELECT c FROM Chamado c " +
                     "LEFT JOIN FETCH c.portal " +
                     "LEFT JOIN FETCH c.categoria " +
                     "LEFT JOIN FETCH c.subtopico " +
                     "LEFT JOIN FETCH c.comentarios " +
                     "WHERE c.status = :status")
       List<Chamado> findByStatusComDetalhes(@Param("status") Status status);

       List<Chamado> findByStatus(Status status);

       List<Chamado> findByPrioridade(Prioridade prioridade);
}
