package AdvertisementService.api;

import AdvertisementService.dto.AdvertisementRequest;
import AdvertisementService.dto.AdvertisementResponse;
import AdvertisementService.service.AdvertisementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/advertisements")
@RequiredArgsConstructor
public class AdvertisementController {

    private final AdvertisementService service;

    // Create
    @PostMapping
    public ResponseEntity<AdvertisementResponse> createAdvertisement(
            @RequestBody AdvertisementRequest request
    ) {
        UUID userId = UUID.randomUUID(); // временный userId
        AdvertisementResponse response = service.createAdvertisement(request, userId);
        return ResponseEntity.ok(response);
    }


    // Read all
    @GetMapping
    public ResponseEntity<List<AdvertisementResponse>> getAllAdvertisements() {
        List<AdvertisementResponse> list = service.getAllAdvertisements();
        return ResponseEntity.ok(list);
    }

    // Read by ID
    @GetMapping("/{id}")
    public ResponseEntity<AdvertisementResponse> getAdvertisementById(@PathVariable UUID id) {
        Optional<AdvertisementResponse> response = service.getAdvertisementById(id);
        return response.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Update
    @PutMapping("/{id}")
    public ResponseEntity<AdvertisementResponse> updateAdvertisement(
            @PathVariable UUID id,
            @RequestBody AdvertisementRequest request
    ) {
        Optional<AdvertisementResponse> response = service.updateAdvertisement(id, request);
        return response.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdvertisement(@PathVariable UUID id) {
        boolean deleted = service.deleteAdvertisement(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
