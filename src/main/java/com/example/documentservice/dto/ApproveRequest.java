package com.example.documentservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ApproveRequest {

    @NotEmpty
    private List<Long> ids;

    @NotBlank
    private String initiator;
}