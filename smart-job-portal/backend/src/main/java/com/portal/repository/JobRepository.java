package com.portal.repository;

import com.portal.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findAllByOrderByCreatedAtDesc();

    List<Job> findByPostedByOrderByCreatedAtDesc(String postedBy);

    @Query("""
           select distinct j from Job j left join j.requiredSkills s
           where lower(j.title) like concat('%', :q, '%')
              or lower(j.company) like concat('%', :q, '%')
              or lower(j.location) like concat('%', :q, '%')
              or lower(s) like concat('%', :q, '%')
           order by j.createdAt desc
           """)
    List<Job> search(@Param("q") String q);
}
