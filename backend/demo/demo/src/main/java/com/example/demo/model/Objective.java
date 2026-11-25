// model/Objective.java
package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "objectives")
public class Objective {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mission_id")
    private Mission mission;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private int orderIndex;

    @Column(nullable = false)
    private boolean requiresProof = false;

    protected Objective() {}

    public Objective(Mission mission, String title, String description, int orderIndex, boolean requiresProof) {
        this.mission = mission;
        this.title = title;
        this.description = description;
        this.orderIndex = orderIndex;
        this.requiresProof = requiresProof;
    }

    public Long getId() { return id; }
    public Mission getMission() { return mission; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getOrderIndex() { return orderIndex; }
    public boolean isRequiresProof() { return requiresProof; }
}