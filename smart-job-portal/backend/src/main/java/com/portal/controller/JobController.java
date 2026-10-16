package com.portal.controller;

import com.portal.dto.JobRequest;
import com.portal.entity.Job;
import com.portal.repository.JobRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
    private final JobRepository jobs;

    public JobController(JobRepository jobs) {
        this.jobs = jobs;
    }

    @GetMapping
    public List<Job> list(@RequestParam(required = false) String q) {
        if (q == null || q.isBlank()) return jobs.findAllByOrderByCreatedAtDesc();
        return jobs.search(q.trim().toLowerCase());
    }

    @GetMapping("/{id}")
    public Job one(@PathVariable Long id) {
        return jobs.findById(id).orElseThrow();
    }

    @GetMapping("/mine")
    public List<Job> mine(Authentication auth) {
        return jobs.findByPostedByOrderByCreatedAtDesc(auth.getName());
    }

    @PostMapping
    public ResponseEntity<Job> create(@Valid @RequestBody JobRequest req, Authentication auth) {
        Job j = new Job();
        j.setTitle(req.title().trim());
        j.setCompany(req.company().trim());
        j.setLocation(req.location().trim());
        j.setDescription(req.description().trim());
        Set<String> skills = req.requiredSkills() == null ? Set.of() : req.requiredSkills().stream()
                .filter(Objects::nonNull)
                .map(s -> s.toLowerCase().trim())
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
        j.setRequiredSkills(skills);
        j.setPostedBy(auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(jobs.save(j));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        Job j = jobs.findById(id).orElseThrow();
        if (!auth.getName().equals(j.getPostedBy())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own jobs");
        }
        jobs.delete(j);
        return ResponseEntity.noContent().build();
    }
}
