package AdvertisementService.service;

import AdvertisementService.dto.AdvertisementRequest;
import AdvertisementService.dto.AdvertisementResponse;
import AdvertisementService.event.AdvertisementCreatedEvent;
import AdvertisementService.event.AdvertisementDeletedEvent;
import AdvertisementService.event.AdvertisementUpdatedEvent;
import AdvertisementService.mapper.AdvertisementMapper;
import AdvertisementService.model.Advertisement;
import AdvertisementService.model.AdvertisementStatus;
import AdvertisementService.repository.AdvertisementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdvertisementService {

    private final AdvertisementRepository repository;
    private final AdvertisementMapper mapper;
    private final ApplicationEventPublisher eventPublisher;
    private final FileStorageService fileStorageService;
    private final CacheService cacheService;

    private String keyById(UUID id) {
        return "ad:" + id;
    }

    // Create
    @Transactional
    public AdvertisementResponse createAdvertisement(AdvertisementRequest request, UUID userId) {
        Advertisement ad = mapper.toEntity(request);
        ad.setUserId(userId);
        ad.setStatus(AdvertisementStatus.ACTIVE);

        Advertisement saved = repository.save(ad);

        eventPublisher.publishEvent(new AdvertisementCreatedEvent(
                saved.getId(),
                saved.getUserId(),
                Instant.now()
        ));

        AdvertisementResponse resp = mapper.toResponse(saved);
        cacheService.save(keyById(saved.getId()), resp);
        cacheService.viewed(saved.getId());

        return resp;
    }

    // Read all
    public List<AdvertisementResponse> getAllAdvertisements() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    // Read by ID
    public Optional<AdvertisementResponse> getAdvertisementById(UUID id) {
        String key = keyById(id);
        AdvertisementResponse cached = (AdvertisementResponse) cacheService.get(key);

        if (cached != null) {
            cacheService.viewed(id);
            return Optional.of(cached);
        }

        return repository.findById(id).map(ad -> {
            AdvertisementResponse resp = mapper.toResponse(ad);
            cacheService.save(key, resp);
            cacheService.viewed(id);
            return resp;
        });
    }

    // Update
    @Transactional
    public Optional<AdvertisementResponse> updateAdvertisement(UUID id, AdvertisementRequest request) {
        return repository.findById(id).map(ad -> {
            ad.setTitle(request.getTitle());
            ad.setDescription(request.getDescription());
            ad.setPrice(request.getPrice());

            Advertisement updated = repository.save(ad);

            eventPublisher.publishEvent(new AdvertisementUpdatedEvent(
                    updated.getId(),
                    Instant.now()
            ));

            AdvertisementResponse resp = mapper.toResponse(updated);
            cacheService.save(keyById(id), resp);

            return resp;
        });
    }

    // Upload thumbnail
    public Optional<AdvertisementResponse> uploadThumbnail(UUID id, MultipartFile file, UUID currentUserId) {
        return repository.findById(id).map(ad -> {
            try {
                String url = fileStorageService.uploadThumbnail(id, file);
                ad.setThumbnailUrl(url);

                Advertisement saved = repository.save(ad);
                AdvertisementResponse resp = mapper.toResponse(saved);

                cacheService.save(keyById(id), resp);
                return resp;
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload thumbnail", e);
            }
        });
    }

    // Delete (soft)
    @Transactional
    public boolean deleteAdvertisement(UUID id) {
        return repository.findById(id).map(ad -> {
            ad.setStatus(AdvertisementStatus.DELETED);
            repository.save(ad);

            eventPublisher.publishEvent(new AdvertisementDeletedEvent(
                    ad.getId(),
                    Instant.now()
            ));

            cacheService.delete(keyById(id));
            return true;
        }).orElse(false);
    }

    // Popular
    public List<AdvertisementResponse> popular(int topN) {
        Set<Object> ids = cacheService.popular(topN);

        Set<UUID> uuidSet = ids.stream()
                .filter(Objects::nonNull)
                .map(o -> UUID.fromString(o.toString()))
                .collect(Collectors.toSet());

        if (uuidSet.isEmpty()) {
            return repository.findAll().stream()
                    .limit(topN)
                    .map(mapper::toResponse)
                    .toList();
        }

        return uuidSet.stream()
                .map(id -> (AdvertisementResponse) cacheService.get(keyById(id)))
                .filter(Objects::nonNull)
                .toList();
    }
}
