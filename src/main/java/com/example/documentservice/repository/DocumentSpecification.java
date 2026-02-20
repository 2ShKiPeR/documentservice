package com.example.documentservice.repository;

import com.example.documentservice.model.Document;
import com.example.documentservice.model.DocumentStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class DocumentSpecification {

    public static Specification<Document> hasStatus(DocumentStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Document> hasAuthor(String author) {
        return (root, query, cb) ->
                author == null ? null : cb.equal(root.get("author"), author);
    }

    public static Specification<Document> createdAfter(LocalDateTime from) {
        return (root, query, cb) ->
                from == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<Document> createdBefore(LocalDateTime to) {
        return (root, query, cb) ->
                to == null ? null : cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }
}
