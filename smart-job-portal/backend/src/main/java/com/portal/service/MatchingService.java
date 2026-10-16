package com.portal.service;

import com.portal.dto.Recommendation;
import com.portal.entity.Job;
import com.portal.entity.Resume;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Resume-to-job matching.
 *
 * 1. Keyword extraction: find known skills in free text.
 * 2. Similarity scoring: TF-IDF cosine similarity between resume text and job text.
 * 3. Final score (0-100) = 60% skill overlap + 40% text similarity.
 */
@Service
public class MatchingService {

    private static final double SKILL_WEIGHT = 0.6;
    private static final double TEXT_WEIGHT = 0.4;

    private static final Set<String> SKILLS = Set.of(
            "java", "spring boot", "spring", "hibernate", "jpa", "junit", "maven", "gradle",
            "mysql", "sql", "postgresql", "mongodb", "redis", "kafka",
            "react", "angular", "vue", "javascript", "typescript", "html", "css", "node",
            "python", "django", "flask", "c++", "c#", "go",
            "docker", "kubernetes", "aws", "azure", "jenkins", "git", "linux",
            "rest", "graphql", "microservices", "machine learning");

    private static final Set<String> STOP = Set.of(
            "the", "and", "an", "of", "to", "in", "for", "with", "on", "is", "are", "as", "at", "by",
            "we", "you", "our", "or", "be", "will", "it", "this", "that", "from", "have", "has");

    // ---------- keyword extraction ----------

    public Set<String> extractSkills(String text) {
        String padded = " " + normalize(text) + " ";
        return SKILLS.stream()
                .filter(skill -> padded.contains(" " + skill + " "))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    // ---------- ranking ----------

    public List<Recommendation> rank(Resume resume, List<Job> jobs, int limit) {
        String resumeText = resume.getExtractedText() == null ? "" : resume.getExtractedText();
        List<String> jobTexts = jobs.stream().map(j -> j.getTitle() + " " + j.getDescription()).toList();

        Map<String, Double> idf = buildIdf(resumeText, jobTexts);
        Map<String, Double> resumeVec = vector(resumeText, idf);

        List<Recommendation> out = new ArrayList<>();
        for (int i = 0; i < jobs.size(); i++) {
            Job job = jobs.get(i);
            double cosine = cosine(resumeVec, vector(jobTexts.get(i), idf));

            Set<String> matched = new TreeSet<>(job.getRequiredSkills());
            matched.retainAll(resume.getSkills());
            Set<String> missing = new TreeSet<>(job.getRequiredSkills());
            missing.removeAll(resume.getSkills());

            out.add(new Recommendation(job, score(job.getRequiredSkills().size(), matched.size(), cosine), matched, missing));
        }
        out.sort(Comparator.comparingDouble(Recommendation::score).reversed());
        return out.size() > limit ? out.subList(0, limit) : out;
    }

    /** Combine skill overlap and text similarity into a 0-100 score. */
    double score(int requiredCount, int matchedCount, double cosine) {
        // A resume is much longer than a job post, so raw cosine values are small; scale gently.
        double text = Math.min(1.0, cosine * 2.5);
        double value = requiredCount == 0
                ? text
                : SKILL_WEIGHT * ((double) matchedCount / requiredCount) + TEXT_WEIGHT * text;
        // Clamp before rounding to guard against floating-point accumulation above 1.0
        value = Math.min(1.0, Math.max(0.0, value));
        return Math.round(value * 1000.0) / 10.0;
    }

    // ---------- TF-IDF ----------

    private String normalize(String text) {
        return text == null ? "" : text.toLowerCase().replaceAll("[^a-z0-9+# ]", " ").replaceAll("\\s+", " ").trim();
    }

    private List<String> tokens(String text) {
        return Arrays.stream(normalize(text).split(" "))
                .filter(w -> w.length() > 1 && !STOP.contains(w))
                .toList();
    }

    private Map<String, Double> buildIdf(String resumeText, List<String> jobTexts) {
        List<String> docs = new ArrayList<>(jobTexts);
        docs.add(resumeText);
        Map<String, Integer> df = new HashMap<>();
        for (String doc : docs) {
            for (String term : new HashSet<>(tokens(doc))) df.merge(term, 1, Integer::sum);
        }
        Map<String, Double> idf = new HashMap<>();
        int n = docs.size();
        df.forEach((term, count) -> idf.put(term, Math.log((n + 1.0) / (count + 1.0)) + 1.0));
        return idf;
    }

    private Map<String, Double> vector(String text, Map<String, Double> idf) {
        Map<String, Double> tf = new HashMap<>();
        for (String t : tokens(text)) tf.merge(t, 1.0, Double::sum);
        tf.replaceAll((term, count) -> count * idf.getOrDefault(term, 1.0));
        return tf;
    }

    double cosine(Map<String, Double> a, Map<String, Double> b) {
        double dot = 0, na = 0, nb = 0;
        for (var e : a.entrySet()) {
            dot += e.getValue() * b.getOrDefault(e.getKey(), 0.0);
            na += e.getValue() * e.getValue();
        }
        for (double v : b.values()) nb += v * v;
        return (na == 0 || nb == 0) ? 0 : dot / (Math.sqrt(na) * Math.sqrt(nb));
    }
}
