package com.example.documentservice.dto;

import com.example.documentservice.model.DocumentStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConcurrentApproveResponse {

    private int success;
    private int conflict;
    private int notFound;
    private DocumentStatus finalStatus;
}
