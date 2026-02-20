package com.example.documentservice.worker;

import com.example.documentservice.model.Document;
import com.example.documentservice.model.DocumentStatus;
import com.example.documentservice.repository.DocumentRepository;
import com.example.documentservice.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubmitWorker {

    private final DocumentRepository documentRepository;
    private final DocumentService documentService;

    @Scheduled(fixedDelay = 10000)
    public void process() {

        List<Document> drafts =
                documentRepository.findTop10ByStatusOrderByCreatedAtAsc(DocumentStatus.DRAFT);

        for (Document doc : drafts) {
            try {
                documentService.submitSingle(doc.getId(), "system-worker");
                log.info("Submitted document {}", doc.getId());
            } catch (Exception e) {
                log.error("Submit failed for {}", doc.getId());
            }
        }
    }
}
