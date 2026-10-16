package com.portal.repository;

import com.portal.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    Optional<Resume> findFirstByUserEmailOrderByUploadedAtDesc(String email);
}
