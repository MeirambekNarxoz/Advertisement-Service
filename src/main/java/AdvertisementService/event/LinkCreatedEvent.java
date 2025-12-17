package AdvertisementService.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class LinkCreatedEvent {
    private final String eventType = "LINK_CREATED";
    private UUID advertisementId;
    private String linkId;
    private String shortCode;
    private Instant timestamp;
}
