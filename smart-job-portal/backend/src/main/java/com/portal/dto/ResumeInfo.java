package com.portal.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record ResumeInfo(String fileName, Set<String> skills, LocalDateTime uploadedAt) {}
