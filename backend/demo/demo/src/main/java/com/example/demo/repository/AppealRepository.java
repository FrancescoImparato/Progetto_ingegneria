// repository/AppealRepository.java
package com.example.demo.repository;

import com.example.demo.model.Appeal;
import com.example.demo.model.AppealTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AppealRepository extends JpaRepository<Appeal, Long> {
    boolean existsByTargetTypeAndTargetId(AppealTargetType targetType, Long targetId);
    List<Appeal> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);
    List<Appeal> findAllByOrderByCreatedAtDesc();
}