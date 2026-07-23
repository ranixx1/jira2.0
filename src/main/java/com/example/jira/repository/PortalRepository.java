package com.example.jira.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.jira.model.Portal;

public interface PortalRepository extends JpaRepository<Portal, Long> {

    List<Portal> findByNome(String nome);
    boolean existsByNome(String nome);
}
