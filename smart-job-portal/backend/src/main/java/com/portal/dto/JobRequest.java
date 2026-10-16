package com.portal.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record JobRequest(
        @NotBlank String title,
        @NotBlank String company,
        @NotBlank String location,
        @NotBlank String description,
        List<String> requiredSkills) {}
