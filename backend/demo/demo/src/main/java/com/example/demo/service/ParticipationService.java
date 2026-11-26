// service/ParticipationService.java
package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.exception.BusinessException;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class ParticipationService {

    private final ParticipationRepository participationRepository;
    private final MissionRepository missionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ParticipationService(ParticipationRepository participationRepository, MissionRepository missionRepository, UserRepository userRepository, NotificationService notificationService) 
    {
        this.participationRepository = participationRepository;
        this.missionRepository = missionRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public ParticipationResponse acceptMission(String userEmail, Long missionId) {
        User user = findUser(userEmail);
        Mission mission = findMission(missionId);

        if (!mission.isPublished()) {
            throw new BusinessException("La missione non è disponibile per l'accettazione", HttpStatus.CONFLICT);
        }
        if (mission.isOwnedBy(user)) {
            throw new BusinessException("Non puoi accettare una missione creata da te", HttpStatus.BAD_REQUEST);
        }

        boolean alreadyActiveOrDone = participationRepository.existsByUserIdAndMissionIdAndStatusIn(
            user.getId(),
            missionId,
            List.of(ParticipationStatus.IN_PROGRESS, ParticipationStatus.PENDING_VERIFICATION, ParticipationStatus.COMPLETED)
        );
        if (alreadyActiveOrDone) {
            throw new BusinessException("Hai già questa missione in corso o completata", HttpStatus.CONFLICT);
        }

        Instant now = Instant.now();
        Participation participation = new Participation(user, mission, now);
        participationRepository.save(participation);
        notificationService.notify(
            user,
            NotificationType.MISSION_ACCEPTED,
            "Hai accettato la missione '" + mission.getTitle() + "'.",
            participation.getId()
        );
        return ParticipationResponse.from(participation, now); 
    }

    @Transactional
    public List<ParticipationResponse> getMyParticipations(String userEmail) {
        User user = findUser(userEmail);
        Instant now = Instant.now();
        List<Participation> list = participationRepository.findByUserIdOrderByAcceptedAtDesc(user.getId());
        list.forEach(p -> p.refreshExpirationIfNeeded(now));
        return list.stream().map(p -> ParticipationResponse.from(p, now)).toList();
    }

    @Transactional
    public ParticipationResponse getParticipationDetail(String userEmail, Long participationId) {
        User user = findUser(userEmail);
        Participation p = findParticipation(participationId);
        if (!p.getUser().getId().equals(user.getId())
            && !p.getMission().isOwnedBy(user)
            && !user.isAdmin()) {
            throw new BusinessException("Accesso non autorizzato", HttpStatus.FORBIDDEN);
        }
        Instant now = Instant.now();
        p.refreshExpirationIfNeeded(now);
        return ParticipationResponse.from(p, now);
    }

    @Transactional
    public ParticipationResponse completeObjective(
        String userEmail, Long participationId, Long objectiveId, SubmitProgressRequest req
    ) {
        User user = findUser(userEmail);
        Participation p = findParticipation(participationId);

        if (!p.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Non sei il partecipante di questa missione", HttpStatus.FORBIDDEN);
        }

        Instant now = Instant.now();
        if (p.refreshExpirationIfNeeded(now)) {
            throw new BusinessException("Tempo scaduto! La missione è ora scaduta.", HttpStatus.CONFLICT);
        }
        if (p.getStatus() != ParticipationStatus.IN_PROGRESS) {
            throw new BusinessException("La missione non è più in corso", HttpStatus.CONFLICT);
        }
        if (p.getStatus() == ParticipationStatus.COMPLETED) {
            notificationService.notify(
                user,
                NotificationType.MISSION_COMPLETED,
                "Missione '" + p.getMission().getTitle() + "' completata! Punti ottenuti: " + p.getScoreAwarded(),
                p.getId()
            );
        }
        if (p.getStatus() == ParticipationStatus.PENDING_VERIFICATION) {
            notificationService.notify(
                p.getMission().getAuthor(),
                NotificationType.PROOF_SUBMITTED,
                user.getNickname() + " ha inviato delle prove da verificare per '" + p.getMission().getTitle() + "'.",
                p.getId()
            );
        }

        ObjectiveProgress progress = p.getProgresses().stream()
            .filter(op -> op.getObjective().getId().equals(objectiveId))
            .findFirst()
            .orElseThrow(() -> new BusinessException("Obiettivo non appartenente a questa missione", HttpStatus.NOT_FOUND));

        String text = req != null ? req.proofText() : null;
        String img = req != null ? req.proofImageUrl() : null;

        if (progress.getObjective().isRequiresProof()
            && (text == null || text.isBlank())
            && (img == null || img.isBlank())) {
            throw new BusinessException("Questo obiettivo richiede una prova (testo o URL immagine)", HttpStatus.BAD_REQUEST);
        }

        progress.submit(text, img, now);
        p.evaluateOutcome(now);
        participationRepository.save(p);
        return ParticipationResponse.from(p, now);
    }

    @Transactional
    public ParticipationResponse abandonMission(String userEmail, Long participationId) {
        User user = findUser(userEmail);
        Participation p = findParticipation(participationId);

        if (!p.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Non sei il partecipante di questa missione", HttpStatus.FORBIDDEN);
        }
        Instant now = Instant.now();
        if (p.refreshExpirationIfNeeded(now)) {
            notificationService.notify(
                user,
                NotificationType.MISSION_EXPIRED,
                "Il tempo per '" + p.getMission().getTitle() + "' è scaduto.",
                p.getId()
            );
        }
        try {
            p.abandon();
        } catch (IllegalStateException ex) {
            throw new BusinessException(ex.getMessage(), HttpStatus.CONFLICT);
        }
        participationRepository.save(p);
        return ParticipationResponse.from(p, now);
    }

    @Transactional
    public List<ParticipationResponse> getMissionParticipationsForAuthor(String authorEmail, Long missionId) {
        User author = findUser(authorEmail);
        Mission mission = findMission(missionId);
        if (!mission.isOwnedBy(author) && !author.isAdmin()) {
            throw new BusinessException("Solo l'autore può vedere le partecipazioni", HttpStatus.FORBIDDEN);
        }
        Instant now = Instant.now();
        List<Participation> list = participationRepository.findByMissionIdOrderByAcceptedAtDesc(missionId);
        list.forEach(p -> p.refreshExpirationIfNeeded(now));
        return list.stream().map(p -> ParticipationResponse.from(p, now)).toList();
    }

    @Transactional
    public ParticipationResponse verifyObjectiveProof(
        String authorEmail, Long participationId, Long objectiveId, VerifyProofRequest req
    ) {
        User author = findUser(authorEmail);
        Participation p = findParticipation(participationId);

        if (!p.getMission().isOwnedBy(author) && !author.isAdmin()) {
            throw new BusinessException("Solo l'autore può verificare le prove", HttpStatus.FORBIDDEN);
        }
        if (p.getStatus() != ParticipationStatus.PENDING_VERIFICATION
            && p.getStatus() != ParticipationStatus.IN_PROGRESS) {
            throw new BusinessException("La partecipazione non è in attesa di verifica", HttpStatus.CONFLICT);
        }

        ObjectiveProgress progress = p.getProgresses().stream()
            .filter(op -> op.getObjective().getId().equals(objectiveId))
            .findFirst()
            .orElseThrow(() -> new BusinessException("Obiettivo non trovato", HttpStatus.NOT_FOUND));

        if (!progress.isCompleted()) {
            throw new BusinessException("L'obiettivo non è ancora stato completato dal partecipante", HttpStatus.BAD_REQUEST);
        }

        Instant now = Instant.now();
        progress.verify(req.approved(), req.note(), now);
        p.evaluateOutcome(now);
        participationRepository.save(p);
        notificationService.notify(
            p.getUser(),
            NotificationType.PROOF_VERIFIED,
            "Verifica prova per '" + p.getMission().getTitle() + "': "
                + (req.approved() ? "APPROVATA" : "RIFIUTATA")
                + " (Stato missione: " + p.getStatus() + ")",
            p.getId()
        );
        return ParticipationResponse.from(p, now);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException("Utente non trovato", HttpStatus.NOT_FOUND));
    }

    private Mission findMission(Long id) {
        return missionRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Missione non trovata", HttpStatus.NOT_FOUND));
    }

    private Participation findParticipation(Long id) {
        return participationRepository.findById(id)
            .orElseThrow(() -> new BusinessException("Partecipazione non trovata", HttpStatus.NOT_FOUND));
    }
}