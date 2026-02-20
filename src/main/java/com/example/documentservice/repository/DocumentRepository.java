package com.example.documentservice.repository;

import com.example.documentservice.model.Document;
import com.example.documentservice.model.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {
    List<Document> findTop10ByStatusOrderByCreatedAtAsc(DocumentStatus status);
    List<Document> findTop5ByStatusOrderByCreatedAtAsc(DocumentStatus status);
}
