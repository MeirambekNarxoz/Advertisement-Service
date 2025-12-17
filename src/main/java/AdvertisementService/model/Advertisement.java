package AdvertisementService.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "advertisements")
public class Advertisement implements Serializable {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    private String title;
    private String description;
    private Integer price;

    private String thumbnailUrl;
    private String shortUrl;

    @Enumerated(EnumType.STRING)
    private AdvertisementStatus status = AdvertisementStatus.ACTIVE;

    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
