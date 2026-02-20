package com.example.documentservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateDocumentRequest {

    @NotBlank
    private String author;

    @NotBlank
    private String title;

    @NotBlank
    private String initiator;
}
