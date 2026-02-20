package com.example.documentservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConcurrentApproveRequest {

    @Min(1)
    private int threads;

    @Min(1)
    private int attempts;

    @NotBlank
    private String initiator;
}
