package com.example.documentservice.repository;

import com.example.documentservice.model.DocumentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentHistoryRepository extends JpaRepository<DocumentHistory, Long> {

    List<DocumentHistory> findByDocumentIdOrderByCreatedAtAsc(Long documentId);

}
