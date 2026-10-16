package com.portal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.TreeSet;

@Entity
@Getter @Setter @NoArgsConstructor
public class Resume {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    private String fileName;

    /** Loaded lazily to avoid pulling hundreds of KB on every resume fetch (C-4). */
    @Basic(fetch = FetchType.LAZY)
    @Column(columnDefinition = "LONGTEXT")
    private String extractedText;

    /** TreeSet gives deterministic, alphabetical ordering in API responses (M-9). */
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> skills = new TreeSet<>();

    /** Set at persist time, not at object-construction time (M-1). */
    @Column(updatable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    void prePersist() {
        uploadedAt = LocalDateTime.now();
    }
}
