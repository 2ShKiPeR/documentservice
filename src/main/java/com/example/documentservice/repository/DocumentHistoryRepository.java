package com.example.documentservice.repository;

import com.example.documentservice.model.DocumentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentHistoryRepository extends JpaRepository<DocumentHistory, Long> { }
