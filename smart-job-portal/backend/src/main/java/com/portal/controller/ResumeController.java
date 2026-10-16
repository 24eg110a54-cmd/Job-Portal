package com.portal.controller;

import com.portal.dto.Recommendation;
import com.portal.dto.ResumeInfo;
import com.portal.entity.Resume;
import com.portal.repository.JobRepository;
import com.portal.repository.ResumeRepository;
import com.portal.repository.UserRepository;
import com.portal.service.MatchingService;
import com.portal.service.ResumeTextExtractor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {
    private final ResumeRepository resumes;
    private final UserRepository users;
    private final JobRepository jobs;
    private final MatchingService matcher;
    private final ResumeTextExtractor extractor;

    public ResumeController(ResumeRepository resumes, UserRepository users, JobRepository jobs,
                            MatchingService matcher, ResumeTextExtractor extractor) {
        this.resumes = resumes;
        this.users = users;
        this.jobs = jobs;
        this.matcher = matcher;
        this.extractor = extractor;
    }

    @PostMapping("/upload")
    @Transactional
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file, Authentication auth) throws IOException {
        if (file.isEmpty()) throw new IllegalArgumentException("Choose a file to upload");
        String text = extractor.extract(file);
        if (text.isBlank()) throw new IllegalArgumentException("No readable text found. Scanned PDFs are not supported.");

        // Sanitise the filename to prevent path-traversal sequences (H-5)
        String rawName = file.getOriginalFilename() == null ? "resume" : file.getOriginalFilename();
        String safeFileName = Paths.get(rawName).getFileName().toString();

        Resume r = new Resume();
        r.setUser(users.findByEmail(auth.getName()).orElseThrow());
        r.setFileName(safeFileName);
        r.setExtractedText(text);
        r.setSkills(matcher.extractSkills(text));
        resumes.save(r);
        return ResponseEntity.ok(new ResumeInfo(r.getFileName(), r.getSkills(), r.getUploadedAt()));
    }

    @GetMapping("/me")
    public ResponseEntity<ResumeInfo> mine(Authentication auth) {
        return resumes.findFirstByUserEmailOrderByUploadedAtDesc(auth.getName())
                .map(r -> ResponseEntity.ok(new ResumeInfo(r.getFileName(), r.getSkills(), r.getUploadedAt())))
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/recommendations")
    public ResponseEntity<?> recommendations(Authentication auth) {
        Resume r = resumes.findFirstByUserEmailOrderByUploadedAtDesc(auth.getName()).orElse(null);
        if (r == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Upload a resume to get recommendations"));
        }
        List<Recommendation> result = matcher.rank(r, jobs.findAll(), 10);
        return ResponseEntity.ok(result);
    }
}
