package com.example.documentservice.repository;

import com.example.documentservice.model.ApprovalRegistry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApprovalRegistryRepository extends JpaRepository<ApprovalRegistry, Long> {

    Optional<ApprovalRegistry> findByDocumentId(Long documentId);

}
