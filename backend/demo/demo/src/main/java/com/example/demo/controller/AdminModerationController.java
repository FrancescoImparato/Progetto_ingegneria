// controller/AdminModerationController.java
package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.service.ModerationService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminModerationController {

    private final ModerationService moderationService;

    public AdminModerationController(ModerationService moderationService) {
        this.moderationService = moderationService;
    }

    @GetMapping("/reports")
    public List<ReportResponse> getAllReports() {
        return moderationService.getAllReports();
    }

    @PostMapping("/reports/{id}/decide")
    public ReportResponse decideReport(Authentication auth, @PathVariable Long id,
                                       @Valid @RequestBody DecideReportRequest req) {
        return moderationService.decideReport(auth.getName(), id, req);
    }

    @GetMapping("/appeals")
    public List<AppealResponse> getAllAppeals() {
        return moderationService.getAllAppeals();
    }

    @PostMapping("/appeals/{id}/decide")
    public AppealResponse decideAppeal(Authentication auth, @PathVariable Long id,
                                       @Valid @RequestBody DecideAppealRequest req) {
        return moderationService.decideAppeal(auth.getName(), id, req);
    }

    @GetMapping("/audit-logs")
    public List<ModerationLogResponse> getAuditLogs() {
        return moderationService.getAuditLogs();
    }
}