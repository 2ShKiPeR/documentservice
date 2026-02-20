package com.example.documentservice.controller;

import com.example.documentservice.dto.*;
import com.example.documentservice.model.DocumentStatus;
import com.example.documentservice.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    public DocumentResponse create(@Valid @RequestBody CreateDocumentRequest request) {
        return documentService.create(request);
    }

    @PostMapping("/submit")
    public List<OperationResult> submit(@Valid @RequestBody SubmitRequest request) {
        return documentService.submitBatch(request);
    }

    @PostMapping("/approve")
    public List<OperationResult> approve(@Valid @RequestBody ApproveRequest request) {
        return documentService.approveBatch(request);
    }

    @PostMapping("/{id}/concurrent-approve")
    public ConcurrentApproveResponse concurrentApprove(
            @PathVariable Long id,
            @Valid @RequestBody ConcurrentApproveRequest request
    ) throws InterruptedException {
        return documentService.concurrentApprove(id, request);
    }

    @GetMapping("/search")
    public Page<DocumentResponse> search(
            @RequestParam(required = false) DocumentStatus status,
            @RequestParam(required = false) String author,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {

        return documentService.search(
                status,
                author,
                from,
                to,
                page,
                size,
                sortBy,
                direction
        );
    }

}
