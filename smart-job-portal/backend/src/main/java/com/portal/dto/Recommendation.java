package com.portal.dto;

import com.portal.entity.Job;

import java.util.Set;

public record Recommendation(Job job, double score, Set<String> matchedSkills, Set<String> missingSkills) {}
