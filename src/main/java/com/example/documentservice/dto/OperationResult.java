package com.example.documentservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OperationResult {

    private Long id;
    private String result; // SUCCESS / CONFLICT / NOT_FOUND
}
