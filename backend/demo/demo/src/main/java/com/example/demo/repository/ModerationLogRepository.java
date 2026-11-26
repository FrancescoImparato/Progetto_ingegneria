// repository/ModerationLogRepository.java
package com.example.demo.repository;

import com.example.demo.model.ModerationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ModerationLogRepository extends JpaRepository<ModerationLog, Long> {
    List<ModerationLog> findAllByOrderByCreatedAtDesc();
}