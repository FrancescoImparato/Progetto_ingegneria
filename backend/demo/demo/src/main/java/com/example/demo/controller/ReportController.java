// controller/ReportController.java
package com.example.demo.controller;

import com.example.demo.dto.CreateReportRequest;
import com.example.demo.dto.ReportResponse;
import com.example.demo.service.ModerationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ModerationService moderationService;

    public ReportController(ModerationService moderationService) {
        this.moderationService = moderationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReportResponse createReport(Authentication auth, @Valid @RequestBody CreateReportRequest req) {
        return moderationService.createReport(auth.getName(), req);
    }

    @GetMapping("/mine")
    public List<ReportResponse> getMyReports(Authentication auth) {
        return moderationService.getMyReports(auth.getName());
    }
}