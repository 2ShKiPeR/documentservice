package com.example.documentservice.service;

import com.example.documentservice.dto.*;
import com.example.documentservice.repository.ApprovalRegistryRepository;
import com.example.documentservice.model.ApprovalRegistry;
import com.example.documentservice.repository.DocumentSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import com.example.documentservice.model.Document;
import com.example.documentservice.model.DocumentStatus;
import com.example.documentservice.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.documentservice.model.DocumentHistory;
import com.example.documentservice.repository.DocumentHistoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentHistoryRepository historyRepository;
    private final ApprovalRegistryRepository approvalRegistryRepository;

    public DocumentResponse create(CreateDocumentRequest request) {

        Document document = Document.builder()
                .number(generateNumber())
                .author(request.getAuthor())
                .title(request.getTitle())
                .status(DocumentStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Document saved = documentRepository.save(document);

        return DocumentResponse.builder()
                .id(saved.getId())
                .number(saved.getNumber())
                .author(saved.getAuthor())
                .title(saved.getTitle())
                .status(saved.getStatus())
                .build();
    }

    private String generateNumber() {
        return "DOC-" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Transactional
    public OperationResult submitSingle(Long id, String initiator) {

        return documentRepository.findById(id)
                .map(document -> {

                    if (document.getStatus() != DocumentStatus.DRAFT) {
                        return OperationResult.builder()
                                .id(id)
                                .result("CONFLICT")
                                .build();
                    }

                    document.setStatus(DocumentStatus.SUBMITTED);
                    document.setUpdatedAt(LocalDateTime.now());
                    documentRepository.save(document);

                    DocumentHistory history = DocumentHistory.builder()
                            .documentId(id)
                            .action("SUBMIT")
                            .actor(initiator)
                            .createdAt(LocalDateTime.now())
                            .build();

                    historyRepository.save(history);

                    return OperationResult.builder()
                            .id(id)
                            .result("SUCCESS")
                            .build();
                })
                .orElse(OperationResult.builder()
                        .id(id)
                        .result("NOT_FOUND")
                        .build());
    }


    public List<OperationResult> submitBatch(SubmitRequest request) {

        List<OperationResult> results = new ArrayList<>();

        for (Long id : request.getIds()) {
            results.add(submitSingle(id, request.getInitiator()));
        }

        return results;
    }

    @Transactional
    public OperationResult approveSingle(Long id, String initiator) {

        return documentRepository.findById(id)
                .map(document -> {

                    if (document.getStatus() != DocumentStatus.SUBMITTED) {
                        return OperationResult.builder()
                                .id(id)
                                .result("CONFLICT")
                                .build();
                    }

                    document.setStatus(DocumentStatus.APPROVED);
                    document.setUpdatedAt(LocalDateTime.now());
                    documentRepository.save(document);

                    // История
                    DocumentHistory history = DocumentHistory.builder()
                            .documentId(id)
                            .action("APPROVE")
                            .actor(initiator)
                            .createdAt(LocalDateTime.now())
                            .build();

                    historyRepository.save(history);

                    // Реестр утверждений
                    ApprovalRegistry registry = ApprovalRegistry.builder()
                            .documentId(id)
                            .approvedBy(initiator)
                            .approvedAt(LocalDateTime.now())
                            .build();

                    approvalRegistryRepository.save(registry);
                    approvalRegistryRepository.flush();
                    return OperationResult.builder()
                            .id(id)
                            .result("SUCCESS")
                            .build();
                })
                .orElse(OperationResult.builder()
                        .id(id)
                        .result("NOT_FOUND")
                        .build());
    }

    public List<OperationResult> approveBatch(ApproveRequest request) {

        List<OperationResult> results = new ArrayList<>();

        for (Long id : request.getIds()) {
            results.add(approveSingle(id, request.getInitiator()));
        }

        return results;
    }

    public ConcurrentApproveResponse concurrentApprove(
            Long id,
            ConcurrentApproveRequest request) throws InterruptedException {

        ExecutorService executor = Executors.newFixedThreadPool(request.getThreads());

        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger conflict = new AtomicInteger(0);
        AtomicInteger notFound = new AtomicInteger(0);

        CountDownLatch latch = new CountDownLatch(request.getThreads());

        for (int i = 0; i < request.getThreads(); i++) {

            executor.submit(() -> {
                try {
                    for (int j = 0; j < request.getAttempts(); j++) {

                        OperationResult result = approveSingle(id, request.getInitiator());

                        switch (result.getResult()) {
                            case "SUCCESS" -> success.incrementAndGet();
                            case "CONFLICT" -> conflict.incrementAndGet();
                            case "NOT_FOUND" -> notFound.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        DocumentStatus finalStatus = documentRepository.findById(id)
                .map(d -> d.getStatus())
                .orElse(null);

        return ConcurrentApproveResponse.builder()
                .success(success.get())
                .conflict(conflict.get())
                .notFound(notFound.get())
                .finalStatus(finalStatus)
                .build();
    }

    public Page<DocumentResponse> search(
            DocumentStatus status,
            String author,
            LocalDateTime from,
            LocalDateTime to,
            int page,
            int size,
            String sortBy,
            String direction
    ) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Document> spec = Specification
                .where(DocumentSpecification.hasStatus(status))
                .and(DocumentSpecification.hasAuthor(author))
                .and(DocumentSpecification.createdAfter(from))
                .and(DocumentSpecification.createdBefore(to));

        Page<Document> result = documentRepository.findAll(spec, pageable);

        return result.map(d ->
                DocumentResponse.builder()
                        .id(d.getId())
                        .number(d.getNumber())
                        .author(d.getAuthor())
                        .title(d.getTitle())
                        .status(d.getStatus())
                        .build()
        );
    }

}
