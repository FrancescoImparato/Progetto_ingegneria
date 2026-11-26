// controller/AppealController.java
package com.example.demo.controller;

import com.example.demo.dto.AppealResponse;
import com.example.demo.dto.CreateAppealRequest;
import com.example.demo.service.ModerationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appeals")
public class AppealController {

    private final ModerationService moderationService;

    public AppealController(ModerationService moderationService) {
        this.moderationService = moderationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppealResponse createAppeal(Authentication auth, @Valid @RequestBody CreateAppealRequest req) {
        return moderationService.createAppeal(auth.getName(), req);
    }

    @GetMapping("/mine")
    public List<AppealResponse> getMyAppeals(Authentication auth) {
        return moderationService.getMyAppeals(auth.getName());
    }
}