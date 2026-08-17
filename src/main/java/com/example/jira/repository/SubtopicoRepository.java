package com.example.jira.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.jira.model.Subtopico;

@Repository
public interface SubtopicoRepository extends JpaRepository<Subtopico,Integer>{
    boolean existsByCategoriaIdAndNome(Integer categoriaId, String nome);
    List<Subtopico> findByCategoria_Id(Integer categoriaId);
}
