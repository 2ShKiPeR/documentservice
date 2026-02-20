package com.example.documentservice.dto;

import com.example.documentservice.model.DocumentStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocumentResponse {

    private Long id;
    private String number;
    private String author;
    private String title;
    private DocumentStatus status;
}
