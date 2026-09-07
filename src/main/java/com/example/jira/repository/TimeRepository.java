package com.example.jira.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.jira.model.Time;

@Repository
public interface TimeRepository extends JpaRepository<Time, Integer> {
    List<Time> findByMembrosContaining(Long userId);

}
