package AdvertisementService.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class AdvertisementDeletedEvent {
    private final String eventType = "ADVERTISEMENT_DELETED";
    private UUID advertisementId;
    private Instant timestamp;
}
