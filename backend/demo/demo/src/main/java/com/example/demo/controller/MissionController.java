// controller/MissionController.java
package com.example.demo.controller;

import com.example.demo.dto.CreateMissionRequest;
import com.example.demo.dto.MissionResponse;
import com.example.demo.model.MissionCategory;
import com.example.demo.model.MissionType;
import com.example.demo.service.MissionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/missions")
public class MissionController {

    private final MissionService missionService;

    public MissionController(MissionService missionService) {
        this.missionService = missionService;
    }

    @GetMapping
    public List<MissionResponse> searchMissions(
        @RequestParam(required = false) String q,
        @RequestParam(required = false) MissionCategory category,
        @RequestParam(required = false) MissionType type
    ) {
        return missionService.searchPublished(q, category, type);
    }

    @GetMapping("/{id}")
    public MissionResponse getDetail(@PathVariable Long id, Authentication auth) {
        String email = (auth != null) ? auth.getName() : null;
        return missionService.getMissionDetail(id, email);
    }

    @GetMapping("/mine")
    public List<MissionResponse> getMyMissions(Authentication auth) {
        return missionService.getMyCreatedMissions(auth.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MissionResponse createMission(Authentication auth, @Valid @RequestBody CreateMissionRequest req) {
        return missionService.createMission(auth.getName(), req);
    }

    @PutMapping("/{id}")
    public MissionResponse updateMission(Authentication auth, @PathVariable Long id,
                                         @Valid @RequestBody CreateMissionRequest req) {
        return missionService.updateMission(auth.getName(), id, req);
    }

    @PostMapping("/{id}/publish")
    public MissionResponse publishMission(Authentication auth, @PathVariable Long id) {
        return missionService.publishMission(auth.getName(), id);
    }
}