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
public class ApproveWorker {

    private final DocumentRepository documentRepository;
    private final DocumentService documentService;

    @Scheduled(fixedDelay = 15000)
    public void process() {

        List<Document> submitted =
                documentRepository.findTop5ByStatusOrderByCreatedAtAsc(DocumentStatus.SUBMITTED);

        for (Document doc : submitted) {
            try {
                documentService.approveSingle(doc.getId(), "system-worker");
                log.info("Approved document {}", doc.getId());
            } catch (Exception e) {
                log.error("Approve failed for {}", doc.getId());
            }
        }
    }
}
