package AdvertisementService.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class AdvertisementCreatedEvent {
    private final String eventType = "ADVERTISEMENT_CREATED";
    private UUID advertisementId;
    private UUID userId;
    private Instant timestamp;
}
