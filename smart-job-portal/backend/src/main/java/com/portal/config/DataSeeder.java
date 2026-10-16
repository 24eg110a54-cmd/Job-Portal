package com.portal.config;

import com.portal.entity.Job;
import com.portal.repository.JobRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/** Inserts a few sample jobs on first start so recommendations work out of the box. */
@Component
public class DataSeeder implements CommandLineRunner {
    private final JobRepository jobs;

    public DataSeeder(JobRepository jobs) {
        this.jobs = jobs;
    }

    @Override
    public void run(String... args) {
        if (jobs.count() > 0) return;
        jobs.save(job("Java Backend Developer", "Northwind Systems", "Hyderabad",
                "Build and maintain REST APIs with Spring Boot and Hibernate. Work with MySQL, write unit tests with JUnit and ship services in Docker.",
                "java", "spring boot", "hibernate", "mysql", "rest", "junit", "docker"));
        jobs.save(job("React Frontend Engineer", "Brightwave", "Bengaluru",
                "Create responsive web apps in React and JavaScript. Consume REST APIs, manage state, and collaborate with designers.",
                "react", "javascript", "html", "css", "rest", "git"));
        jobs.save(job("Full Stack Developer", "Orbit Labs", "Remote",
                "Own features end to end: React user interfaces, Spring Boot services and MySQL schemas. Deploy with Docker on AWS.",
                "java", "spring boot", "react", "mysql", "docker", "aws", "git"));
        jobs.save(job("Python Data Engineer", "Datastream", "Pune",
                "Design data pipelines in Python and SQL, work with Kafka and PostgreSQL, and automate workflows on Linux.",
                "python", "sql", "postgresql", "kafka", "linux"));
        jobs.save(job("DevOps Engineer", "CloudNest", "Chennai",
                "Automate CI/CD with Jenkins and Git, run workloads on Kubernetes and Docker, and manage infrastructure on AWS.",
                "docker", "kubernetes", "jenkins", "aws", "linux", "git"));
        jobs.save(job("Microservices Developer", "Finlytics", "Mumbai",
                "Develop Java microservices using Spring Boot, Kafka and Redis. Experience with REST design and MongoDB is a plus.",
                "java", "spring boot", "microservices", "kafka", "redis", "mongodb", "rest"));
    }

    private Job job(String title, String company, String location, String description, String... skills) {
        Job j = new Job();
        j.setTitle(title);
        j.setCompany(company);
        j.setLocation(location);
        j.setDescription(description);
        j.setRequiredSkills(new HashSet<>(Set.of(skills)));
        j.setPostedBy("seed@portal.local");
        return j;
    }
}
