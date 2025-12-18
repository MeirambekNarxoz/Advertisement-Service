package AdvertisementService.api;

import AdvertisementService.dto.AdvertisementRequest;
import AdvertisementService.dto.AdvertisementResponse;
import AdvertisementService.service.AdvertisementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/advertisements")
@RequiredArgsConstructor
public class AdvertisementController {

    private final AdvertisementService service;

    // Create
    @PostMapping(
            consumes = {"multipart/form-data"},
            produces = "application/json"
    )
    public ResponseEntity<AdvertisementResponse> createWithThumbnail(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam Integer price,
            @RequestParam("file") MultipartFile file
    ) {
        UUID userId = UUID.randomUUID(); // временно

        AdvertisementRequest request = new AdvertisementRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setPrice(price);

        AdvertisementResponse created = service.createAdvertisement(request);

        UUID adId = created.getId();
        AdvertisementResponse withThumb = service
                .uploadThumbnail(adId, file, userId)
                .orElse(created);

        return ResponseEntity.ok(withThumb);
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
// Обновление объявления + (опционально) новое фото
    @PutMapping(
            value = "/{id}",
            consumes = {"multipart/form-data"},
            produces = "application/json"
    )
    public ResponseEntity<AdvertisementResponse> updateWithThumbnail(
            @PathVariable UUID id,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam Integer price,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        AdvertisementRequest request = new AdvertisementRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setPrice(price);

        Optional<AdvertisementResponse> response =
                service.updateAdvertisement(id, request, file);

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
