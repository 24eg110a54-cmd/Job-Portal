package com.portal.service;

import com.portal.dto.Recommendation;
import com.portal.entity.Job;
import com.portal.entity.Resume;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MatchingServiceTest {
    private final MatchingService matcher = new MatchingService();

    @Test
    void extractsKnownSkills() {
        Set<String> skills = matcher.extractSkills("Built REST APIs in Java with Spring Boot and MySQL. Deployed using Docker.");
        assertTrue(skills.containsAll(Set.of("java", "spring boot", "mysql", "docker", "rest")));
        assertFalse(skills.contains("python"));
    }

    @Test
    void ranksRelevantJobFirst() {
        String text = "Java developer. Spring Boot, Hibernate, MySQL, REST APIs, JUnit, Docker.";
        Resume resume = new Resume();
        resume.setExtractedText(text);
        resume.setSkills(matcher.extractSkills(text));

        Job java = job("Java Backend Developer", "Build REST APIs with Spring Boot and MySQL.",
                "java", "spring boot", "mysql", "rest");
        Job python = job("Python Data Engineer", "Build data pipelines in Python and Kafka.",
                "python", "kafka", "sql");

        List<Recommendation> ranked = matcher.rank(resume, List.of(python, java), 10);

        assertEquals("Java Backend Developer", ranked.get(0).job().getTitle());
        assertTrue(ranked.get(0).score() > ranked.get(1).score());
        assertTrue(ranked.get(0).missingSkills().isEmpty());
        assertTrue(ranked.get(1).matchedSkills().isEmpty());
    }

    private Job job(String title, String description, String... skills) {
        Job j = new Job();
        j.setTitle(title);
        j.setDescription(description);
        j.setRequiredSkills(new HashSet<>(Set.of(skills)));
        return j;
    }
}
