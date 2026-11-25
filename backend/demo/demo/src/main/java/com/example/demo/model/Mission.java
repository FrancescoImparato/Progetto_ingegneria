// model/Mission.java
package com.example.demo.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "missions")
public class Mission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MissionCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MissionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MissionStatus status = MissionStatus.DRAFT;

    private Integer durationMinutes; // obbligatorio solo se SECRET_TIMED

    @Column(nullable = false)
    private int points = 100;

    @Column(length = 500)
    private String participationConditions;

    private Double latitude;
    private Double longitude;

    @Column(length = 200)
    private String locationName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id")
    private User author;

    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<Objective> objectives = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Mission() {}

    public Mission(String title, String description, MissionCategory category, MissionType type,
                   Integer durationMinutes, int points, String participationConditions,
                   Double latitude, Double longitude, String locationName, User author) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.type = type;
        this.durationMinutes = (type == MissionType.SECRET_TIMED) ? durationMinutes : null;
        this.points = points;
        this.participationConditions = participationConditions;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationName = locationName;
        this.author = author;
    }

    // Metodi di dominio OOP (utili anche per il Class Diagram di Design!)
    public void addObjective(String title, String description, int orderIndex, boolean requiresProof) {
        this.objectives.add(new Objective(this, title, description, orderIndex, requiresProof));
    }

    public void clearAndSetObjectives(List<Objective> newObjectives) {
        this.objectives.clear();
        this.objectives.addAll(newObjectives);
    }

    public boolean isOwnedBy(User user) {
        return user != null && this.author.getId().equals(user.getId());
    }

    public boolean isSecretTimed() {
        return this.type == MissionType.SECRET_TIMED;
    }

    public boolean isPublished() {
        return this.status == MissionStatus.PUBLISHED;
    }

    public void publish() {
        if (this.objectives.isEmpty()) {
            throw new IllegalStateException("La missione deve avere almeno un obiettivo per essere pubblicata");
        }
        if (this.status == MissionStatus.SUSPENDED || this.status == MissionStatus.REMOVED) {
            throw new IllegalStateException("Una missione moderata non può essere ripubblicata direttamente");
        }
        this.status = MissionStatus.PUBLISHED;
    }

    public void suspend() {
        this.status = MissionStatus.SUSPENDED;
    }

    public void remove() {
        this.status = MissionStatus.REMOVED;
    }

    public void restoreToPublished() {
        this.status = MissionStatus.PUBLISHED;
    }

    public void updateDetails(String title, String description, MissionCategory category,
                              MissionType type, Integer durationMinutes, int points,
                              String participationConditions, Double latitude, Double longitude,
                              String locationName) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.type = type;
        this.durationMinutes = (type == MissionType.SECRET_TIMED) ? durationMinutes : null;
        this.points = points;
        this.participationConditions = participationConditions;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationName = locationName;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public MissionCategory getCategory() { return category; }
    public MissionType getType() { return type; }
    public MissionStatus getStatus() { return status; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public int getPoints() { return points; }
    public String getParticipationConditions() { return participationConditions; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public String getLocationName() { return locationName; }
    public User getAuthor() { return author; }
    public List<Objective> getObjectives() { return Collections.unmodifiableList(objectives); }
    public Instant getCreatedAt() { return createdAt; }
}