package com.example.demo.repository;

import com.example.demo.model.Participation;
import com.example.demo.model.ParticipationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {

    boolean existsByMissionId(Long missionId);

    boolean existsByUserIdAndMissionIdAndStatusIn(
        Long userId, Long missionId, Collection<ParticipationStatus> statuses
    );

    List<Participation> findByUserIdOrderByAcceptedAtDesc(Long userId);

    List<Participation> findByMissionIdOrderByAcceptedAtDesc(Long missionId);
}