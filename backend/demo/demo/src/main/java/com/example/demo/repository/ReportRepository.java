// repository/ReportRepository.java
package com.example.demo.repository;

import com.example.demo.model.Report;
import com.example.demo.model.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    boolean existsByReporterIdAndMissionIdAndStatus(Long reporterId, Long missionId, ReportStatus status);
    List<Report> findByReporterIdOrderByCreatedAtDesc(Long reporterId);
    List<Report> findAllByOrderByCreatedAtDesc();
}