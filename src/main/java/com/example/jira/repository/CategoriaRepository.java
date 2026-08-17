package com.example.jira.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.jira.model.Categoria;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    @Query("SELECT c FROM Categoria c WHERE c.time.id IN :timesIds")
    List<Categoria> findByTimesIds(@Param("timesIds") List<Integer> timesIds);

    boolean existsByPortalIdAndNome(Long portalId, String nome);

    List<Categoria> findByPortal_Id(Long portalId);
}