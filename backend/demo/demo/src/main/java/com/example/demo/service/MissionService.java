// service/MissionService.java
package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.exception.BusinessException;
import com.example.demo.model.*;
import com.example.demo.repository.MissionRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class MissionService {

    private final MissionRepository missionRepository;
    private final UserRepository userRepository;

    public MissionService(MissionRepository missionRepository, UserRepository userRepository) {
        this.missionRepository = missionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public MissionResponse createMission(String userEmail, CreateMissionRequest req) {
        User author = findUserByEmail(userEmail);
        validateMissionType(req.type(), req.durationMinutes());

        Mission mission = new Mission(
            req.title().trim(),
            req.description().trim(),
            req.category(),
            req.type(),
            req.durationMinutes(),
            req.points(),
            req.participationConditions(),
            req.latitude(),
            req.longitude(),
            req.locationName(),
            author
        );

        for (int i = 0; i < req.objectives().size(); i++) {
            ObjectiveRequest o = req.objectives().get(i);
            mission.addObjective(o.title().trim(), o.description(), i + 1, o.requiresProof());
        }

        if (req.publishImmediately()) {
            mission.publish();
        }

        missionRepository.save(mission);
        return MissionResponse.from(mission, true);
    }

    @Transactional
    public MissionResponse updateMission(String userEmail, Long missionId, CreateMissionRequest req) {
        User user = findUserByEmail(userEmail);
        Mission mission = findMissionById(missionId);

        if (!mission.isOwnedBy(user)) {
            throw new BusinessException("Puoi modificare solo le tue missioni", HttpStatus.FORBIDDEN);
        }
        if (mission.getStatus() == MissionStatus.SUSPENDED || mission.getStatus() == MissionStatus.REMOVED) {
            throw new BusinessException("Una missione moderata non può essere modificata", HttpStatus.CONFLICT);
        }
        // Nota: nel Blocco 3 aggiungeremo qui il controllo "nessun partecipante ha già iniziato la missione"

        validateMissionType(req.type(), req.durationMinutes());
        mission.updateDetails(
            req.title().trim(), req.description().trim(), req.category(),
            req.type(), req.durationMinutes(), req.points(),
            req.participationConditions(), req.latitude(), req.longitude(), req.locationName()
        );

        List<Objective> updatedObjectives = new ArrayList<>();
        for (int i = 0; i < req.objectives().size(); i++) {
            ObjectiveRequest o = req.objectives().get(i);
            updatedObjectives.add(new Objective(mission, o.title().trim(), o.description(), i + 1, o.requiresProof()));
        }
        mission.clearAndSetObjectives(updatedObjectives);

        if (req.publishImmediately() && mission.getStatus() == MissionStatus.DRAFT) {
            mission.publish();
        }

        missionRepository.save(mission);
        return MissionResponse.from(mission, true);
    }

    @Transactional
    public MissionResponse publishMission(String userEmail, Long missionId) {
        User user = findUserByEmail(userEmail);
        Mission mission = findMissionById(missionId);

        if (!mission.isOwnedBy(user)) {
            throw new BusinessException("Non sei l'autore di questa missione", HttpStatus.FORBIDDEN);
        }
        try {
            mission.publish();
        } catch (IllegalStateException ex) {
            throw new BusinessException(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
        missionRepository.save(mission);
        return MissionResponse.from(mission, true);
    }

    @Transactional(readOnly = true)
    public List<MissionResponse> searchPublished(String q, MissionCategory category, MissionType type) {
        return missionRepository.searchPublished(MissionStatus.PUBLISHED, q, category, type)
            .stream()
            .map(m -> MissionResponse.from(m, !m.isSecretTimed()))
            .toList();
    }

    @Transactional(readOnly = true)
    public MissionResponse getMissionDetail(Long missionId, String requesterEmailOrNull) {
        Mission mission = findMissionById(missionId);

        boolean isAuthorOrAdmin = false;
        if (requesterEmailOrNull != null) {
            User requester = userRepository.findByEmail(requesterEmailOrNull).orElse(null);
            isAuthorOrAdmin = requester != null && (mission.isOwnedBy(requester) || requester.isAdmin());
        }

        if (!mission.isPublished() && !isAuthorOrAdmin) {
            throw new BusinessException("Missione non disponibile", HttpStatus.NOT_FOUND);
        }

        boolean showObjectives = !mission.isSecretTimed() || isAuthorOrAdmin;
        return MissionResponse.from(mission, showObjectives);
    }

    @Transactional(readOnly = true)
    public List<MissionResponse> getMyCreatedMissions(String userEmail) {
        User author = findUserByEmail(userEmail);
        return missionRepository.findByAuthorIdOrderByCreatedAtDesc(author.getId())
            .stream()
            .map(m -> MissionResponse.from(m, true))
            .toList();
    }

    private void validateMissionType(MissionType type, Integer durationMinutes) {
        if (type == MissionType.SECRET_TIMED && (durationMinutes == null || durationMinutes <= 0)) {
            throw new BusinessException("Le missioni segrete a tempo richiedono una durata in minuti", HttpStatus.BAD_REQUEST);
        }
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException("Utente non trovato", HttpStatus.NOT_FOUND));
    }

    private Mission findMissionById(Long id) {
        return missionRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Missione non trovata", HttpStatus.NOT_FOUND));
    }
}