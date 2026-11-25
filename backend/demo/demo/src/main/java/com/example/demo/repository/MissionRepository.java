package com.example.demo.repository;

import com.example.demo.model.Mission;
import com.example.demo.model.MissionCategory;
import com.example.demo.model.MissionStatus;
import com.example.demo.model.MissionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MissionRepository extends JpaRepository<Mission, Long> {

    List<Mission> findByAuthorIdOrderByCreatedAtDesc(Long authorId);

    @Query("""
        SELECT m FROM Mission m
        WHERE m.status = :status
          AND (:category IS NULL OR m.category = :category)
          AND (:type IS NULL OR m.type = :type)
          AND (:q IS NULL OR :q = ''
               OR LOWER(m.title) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(m.description) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(COALESCE(m.locationName, '')) LIKE LOWER(CONCAT('%', :q, '%')))
        ORDER BY m.createdAt DESC
    """)
    List<Mission> searchPublished(
        @Param("status") MissionStatus status,
        @Param("q") String query,
        @Param("category") MissionCategory category,
        @Param("type") MissionType type
    );
}