package AdvertisementService.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
public class AdvertisementEvent {
    private String eventType;
    private UUID advertisementId;
    private UUID userId;
    private Instant timestamp;
}
