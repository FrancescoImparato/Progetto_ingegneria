// service/ModerationService.java
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
public class ModerationService {

    private final ReportRepository reportRepository;
    private final AppealRepository appealRepository;
    private final ModerationLogRepository logRepository;
    private final MissionRepository missionRepository;
    private final ParticipationRepository participationRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ModerationService(
        ReportRepository reportRepository,
        AppealRepository appealRepository,
        ModerationLogRepository logRepository,
        MissionRepository missionRepository,
        ParticipationRepository participationRepository,
        UserRepository userRepository,
        NotificationService notificationService
    ) {
        this.reportRepository = reportRepository;
        this.appealRepository = appealRepository;
        this.logRepository = logRepository;
        this.missionRepository = missionRepository;
        this.participationRepository = participationRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    // --- R13: Segnalazioni ---
    @Transactional
    public ReportResponse createReport(String userEmail, CreateReportRequest req) {
        User reporter = findUser(userEmail);
        Mission mission = missionRepository.findById(req.missionId())
            .orElseThrow(() -> new BusinessException("Missione non trovata", HttpStatus.NOT_FOUND));

        if (mission.isOwnedBy(reporter)) {
            throw new BusinessException("Non puoi segnalare una tua missione", HttpStatus.BAD_REQUEST);
        }
        if (reportRepository.existsByReporterIdAndMissionIdAndStatus(reporter.getId(), mission.getId(), ReportStatus.OPEN)) {
            throw new BusinessException("Hai già una segnalazione aperta per questa missione", HttpStatus.CONFLICT);
        }

        Report report = reportRepository.save(new Report(reporter, mission, req.reason(), req.details().trim()));
        return ReportResponse.from(report);
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> getMyReports(String userEmail) {
        User reporter = findUser(userEmail);
        return reportRepository.findByReporterIdOrderByCreatedAtDesc(reporter.getId())
            .stream().map(ReportResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> getAllReports() {
        return reportRepository.findAllByOrderByCreatedAtDesc()
            .stream().map(ReportResponse::from).toList();
    }

    // --- R14: Moderazione Admin ---
    @Transactional
    public ReportResponse decideReport(String adminEmail, Long reportId, DecideReportRequest req) {
        User admin = findUser(adminEmail);
        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new BusinessException("Segnalazione non trovata", HttpStatus.NOT_FOUND));

        Instant now = Instant.now();
        try {
            report.decide(admin, req.action(), req.decisionNote().trim(), now);
        } catch (IllegalStateException ex) {
            throw new BusinessException(ex.getMessage(), HttpStatus.CONFLICT);
        }

        Mission mission = report.getMission();
        if (req.action() == ModerationAction.SUSPEND_MISSION) {
            mission.suspend();
            notificationService.notify(
                mission.getAuthor(),
                NotificationType.MISSION_MODERATED,
                "La tua missione '" + mission.getTitle() + "' è stata sospesa: " + req.decisionNote(),
                report.getId()
            );
        } else if (req.action() == ModerationAction.REMOVE_MISSION) {
            mission.remove();
            notificationService.notify(
                mission.getAuthor(),
                NotificationType.MISSION_MODERATED,
                "La tua missione '" + mission.getTitle() + "' è stata rimossa: " + req.decisionNote(),
                report.getId()
            );
        }

        notificationService.notify(
            report.getReporter(),
            NotificationType.REPORT_DECIDED,
            "La tua segnalazione su '" + mission.getTitle() + "' è stata esaminata (" + report.getStatus() + ").",
            report.getId()
        );

        logRepository.save(new ModerationLog(
            admin, req.action().name(), "REPORT", report.getId(), req.decisionNote().trim()
        ));

        reportRepository.save(report);
        return ReportResponse.from(report);
    }

    // --- R15: Contestazioni e riesame ---
    @Transactional
    public AppealResponse createAppeal(String userEmail, CreateAppealRequest req) {
        User requester = findUser(userEmail);

        if (appealRepository.existsByTargetTypeAndTargetId(req.targetType(), req.targetId())) {
            throw new BusinessException("Esiste già una richiesta di riesame per questo elemento", HttpStatus.CONFLICT);
        }

        if (req.targetType() == AppealTargetType.MODERATION_REPORT) {
            Report report = reportRepository.findById(req.targetId())
                .orElseThrow(() -> new BusinessException("Segnalazione non trovata", HttpStatus.NOT_FOUND));
            if (!report.getMission().isOwnedBy(requester)) {
                throw new BusinessException("Solo l'autore della missione può contestare la moderazione", HttpStatus.FORBIDDEN);
            }
            if (report.getStatus() != ReportStatus.ACTION_TAKEN) {
                throw new BusinessException("Puoi chiedere il riesame solo per sanzioni effettive", HttpStatus.BAD_REQUEST);
            }
        } else {
            Participation p = participationRepository.findById(req.targetId())
                .orElseThrow(() -> new BusinessException("Partecipazione non trovata", HttpStatus.NOT_FOUND));
            if (!p.getUser().getId().equals(requester.getId())) {
                throw new BusinessException("Solo il partecipante coinvolto può contestare l'esito", HttpStatus.FORBIDDEN);
            }
            if (p.getStatus() != ParticipationStatus.FAILED && p.getStatus() != ParticipationStatus.EXPIRED) {
                throw new BusinessException("Puoi contestare solo un esito negativo o scaduto", HttpStatus.BAD_REQUEST);
            }
        }

        Appeal appeal = appealRepository.save(
            new Appeal(requester, req.targetType(), req.targetId(), req.reason().trim())
        );
        return AppealResponse.from(appeal);
    }

    @Transactional(readOnly = true)
    public List<AppealResponse> getMyAppeals(String userEmail) {
        User user = findUser(userEmail);
        return appealRepository.findByRequesterIdOrderByCreatedAtDesc(user.getId())
            .stream().map(AppealResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<AppealResponse> getAllAppeals() {
        return appealRepository.findAllByOrderByCreatedAtDesc()
            .stream().map(AppealResponse::from).toList();
    }

    @Transactional
    public AppealResponse decideAppeal(String adminEmail, Long appealId, DecideAppealRequest req) {
        User admin = findUser(adminEmail);
        Appeal appeal = appealRepository.findById(appealId)
            .orElseThrow(() -> new BusinessException("Richiesta di riesame non trovata", HttpStatus.NOT_FOUND));

        Instant now = Instant.now();
        try {
            appeal.decide(admin, req.accepted(), req.decisionNote().trim(), now);
        } catch (IllegalStateException ex) {
            throw new BusinessException(ex.getMessage(), HttpStatus.CONFLICT);
        }

        if (req.accepted()) {
            if (appeal.getTargetType() == AppealTargetType.MODERATION_REPORT) {
                Report report = reportRepository.findById(appeal.getTargetId())
                    .orElseThrow(() -> new BusinessException("Segnalazione collegata non trovata", HttpStatus.NOT_FOUND));
                report.getMission().restoreToPublished();
            } else {
                Participation p = participationRepository.findById(appeal.getTargetId())
                    .orElseThrow(() -> new BusinessException("Partecipazione collegata non trovata", HttpStatus.NOT_FOUND));
                p.overrideOutcomeAfterAppeal(true, now);
            }
        }

        notificationService.notify(
            appeal.getRequester(),
            NotificationType.APPEAL_DECIDED,
            "La tua richiesta di riesame #" + appeal.getId() + " è stata "
                + (req.accepted() ? "ACCOLTA" : "RESPINTA") + ": " + req.decisionNote(),
            appeal.getId()
        );

        logRepository.save(new ModerationLog(
            admin,
            req.accepted() ? "APPEAL_ACCEPTED" : "APPEAL_REJECTED",
            "APPEAL",
            appeal.getId(),
            req.decisionNote().trim()
        ));

        appealRepository.save(appeal);
        return AppealResponse.from(appeal);
    }

    @Transactional(readOnly = true)
    public List<ModerationLogResponse> getAuditLogs() {
        return logRepository.findAllByOrderByCreatedAtDesc()
            .stream().map(ModerationLogResponse::from).toList();
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException("Utente non trovato", HttpStatus.NOT_FOUND));
    }
}