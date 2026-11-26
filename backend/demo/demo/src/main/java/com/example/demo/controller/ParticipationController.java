// controller/ParticipationController.java
package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.service.ParticipationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ParticipationController {

    private final ParticipationService participationService;

    public ParticipationController(ParticipationService participationService) {
        this.participationService = participationService;
    }

    @PostMapping("/missions/{missionId}/accept")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationResponse acceptMission(Authentication auth, @PathVariable Long missionId) {
        return participationService.acceptMission(auth.getName(), missionId);
    }

    @GetMapping("/participations/mine")
    public List<ParticipationResponse> getMyParticipations(Authentication auth) {
        return participationService.getMyParticipations(auth.getName());
    }

    @GetMapping("/participations/{id}")
    public ParticipationResponse getDetail(Authentication auth, @PathVariable Long id) {
        return participationService.getParticipationDetail(auth.getName(), id);
    }

    @PostMapping("/participations/{id}/objectives/{objectiveId}/complete")
    public ParticipationResponse completeObjective(
        Authentication auth,
        @PathVariable Long id,
        @PathVariable Long objectiveId,
        @Valid @RequestBody(required = false) SubmitProgressRequest req
    ) {
        return participationService.completeObjective(auth.getName(), id, objectiveId, req);
    }

    @PostMapping("/participations/{id}/abandon")
    public ParticipationResponse abandonMission(Authentication auth, @PathVariable Long id) {
        return participationService.abandonMission(auth.getName(), id);
    }

    @GetMapping("/missions/{missionId}/participations")
    public List<ParticipationResponse> getMissionParticipations(Authentication auth, @PathVariable Long missionId) {
        return participationService.getMissionParticipationsForAuthor(auth.getName(), missionId);
    }

    @PostMapping("/participations/{id}/objectives/{objectiveId}/verify")
    public ParticipationResponse verifyProof(
        Authentication auth,
        @PathVariable Long id,
        @PathVariable Long objectiveId,
        @Valid @RequestBody VerifyProofRequest req
    ) {
        return participationService.verifyObjectiveProof(auth.getName(), id, objectiveId, req);
    }
}