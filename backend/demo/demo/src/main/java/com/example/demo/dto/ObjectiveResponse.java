// dto/ObjectiveResponse.java
package com.example.demo.dto;

import com.example.demo.model.Objective;

public record ObjectiveResponse(
    Long id, String title, String description, int orderIndex, boolean requiresProof
) {
    public static ObjectiveResponse from(Objective o) {
        return new ObjectiveResponse(o.getId(), o.getTitle(), o.getDescription(), o.getOrderIndex(), o.isRequiresProof());
    }
}