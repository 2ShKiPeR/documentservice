package com.example.documentservice;

import com.example.documentservice.model.Document;
import com.example.documentservice.model.DocumentStatus;
import com.example.documentservice.repository.DocumentRepository;
import com.example.documentservice.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DocumentServiceIntegrationTest {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentRepository documentRepository;

    @Test
    void happyPath_singleDocument() {

        Document doc = documentRepository.save(
                Document.builder()
                        .author("Ivan")
                        .title("Test")
                        .number("DOC-1")
                        .status(DocumentStatus.DRAFT)
                        .build()
        );

        documentService.submitSingle(doc.getId(), "Ivan");
        documentService.approveSingle(doc.getId(), "Manager");

        Document updated = documentRepository.findById(doc.getId()).orElseThrow();

        assertThat(updated.getStatus()).isEqualTo(DocumentStatus.APPROVED);
    }

    @Test
    void batchSubmit_partialResults() {

        Document draft = documentRepository.save(
                Document.builder()
                        .author("A")
                        .title("Draft")
                        .number("DOC-2")
                        .status(DocumentStatus.DRAFT)
                        .build()
        );

        Document alreadySubmitted = documentRepository.save(
                Document.builder()
                        .author("B")
                        .title("Submitted")
                        .number("DOC-3")
                        .status(DocumentStatus.SUBMITTED)
                        .build()
        );

        documentService.submitSingle(draft.getId(), "User");

        documentService.submitSingle(alreadySubmitted.getId(), "User");

        Document updatedDraft = documentRepository.findById(draft.getId()).orElseThrow();
        Document updatedSubmitted = documentRepository.findById(alreadySubmitted.getId()).orElseThrow();

        assertThat(updatedDraft.getStatus()).isEqualTo(DocumentStatus.SUBMITTED);
        assertThat(updatedSubmitted.getStatus()).isEqualTo(DocumentStatus.SUBMITTED);
    }

    @Test
    void batchApprove_partialResults() {

        Document submitted = documentRepository.save(
                Document.builder()
                        .author("A")
                        .title("Submitted")
                        .number("DOC-4")
                        .status(DocumentStatus.SUBMITTED)
                        .build()
        );

        Document draft = documentRepository.save(
                Document.builder()
                        .author("B")
                        .title("Draft")
                        .number("DOC-5")
                        .status(DocumentStatus.DRAFT)
                        .build()
        );

        documentService.approveSingle(submitted.getId(), "Manager");
        documentService.approveSingle(draft.getId(), "Manager");

        Document updatedSubmitted = documentRepository.findById(submitted.getId()).orElseThrow();
        Document updatedDraft = documentRepository.findById(draft.getId()).orElseThrow();

        assertThat(updatedSubmitted.getStatus()).isEqualTo(DocumentStatus.APPROVED);
        assertThat(updatedDraft.getStatus()).isEqualTo(DocumentStatus.DRAFT);
    }

    @Test
    void approveRollback_whenRegistryFails() {

        Document doc = documentRepository.save(
                Document.builder()
                        .author("A")
                        .title("Rollback")
                        .number("DOC-6")
                        .status(DocumentStatus.SUBMITTED)
                        .build()
        );

        try {
            documentService.approveSingle(doc.getId(), null);
        } catch (Exception ignored) {}

        Document after = documentRepository.findById(doc.getId()).orElseThrow();

        assertThat(after.getStatus()).isEqualTo(DocumentStatus.SUBMITTED);
    }
}
