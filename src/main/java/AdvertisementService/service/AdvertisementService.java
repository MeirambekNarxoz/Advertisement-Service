package AdvertisementService.service;

import AdvertisementService.dto.AdvertisementRequest;
import AdvertisementService.dto.AdvertisementResponse;
import AdvertisementService.event.AdvertisementCreatedEvent;
import AdvertisementService.event.AdvertisementDeletedEvent;
import AdvertisementService.event.AdvertisementEventPublisher;
import AdvertisementService.event.AdvertisementUpdatedEvent;
import AdvertisementService.mapper.AdvertisementMapper;
import AdvertisementService.model.Advertisement;
import AdvertisementService.model.AdvertisementStatus;
import AdvertisementService.repository.AdvertisementRepository;
import AdvertisementService.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
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
    private final AdvertisementEventPublisher advertisementEventPublisher;
    private final FileStorageService fileStorageService;
    private final CacheService cacheService;

    private String keyById(UUID id) {
        return "ad:" + id;
    }

    // Create
    @Transactional
    public AdvertisementResponse createAdvertisement(AdvertisementRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();

        Advertisement ad = mapper.toEntity(request);
        ad.setUserId(userId);
        ad.setStatus(AdvertisementStatus.ACTIVE);

        Advertisement saved = repository.save(ad);

        advertisementEventPublisher.publishAdvertisementCreated(
                new AdvertisementCreatedEvent(
                        saved.getId(),
                        saved.getUserId(),
                        Instant.now()
                )
        );

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
// Обновление полей + замена фото
    @Transactional
    public Optional<AdvertisementResponse> updateAdvertisement(
            UUID id,
            AdvertisementRequest request,
            MultipartFile newThumbnail // может быть null
    ) {
        return repository.findById(id).map(ad -> {
            // Обновляем текстовые поля
            ad.setTitle(request.getTitle());
            ad.setDescription(request.getDescription());
            ad.setPrice(request.getPrice());

            // Если пришёл новый файл – меняем картинку
            if (newThumbnail != null && !newThumbnail.isEmpty()) {
                // 1. Удаляем старое фото (если было)
                String oldThumbnailUrl = ad.getThumbnailUrl();
                if (oldThumbnailUrl != null && !oldThumbnailUrl.isBlank()) {
                    fileStorageService.deleteByUrl(oldThumbnailUrl);
                }

                // 2. Загружаем новое фото
                try {
                    String newUrl = fileStorageService.uploadThumbnail(id, newThumbnail);
                    ad.setThumbnailUrl(newUrl);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to upload new thumbnail", e);
                }
            }

            Advertisement updated = repository.save(ad);

            advertisementEventPublisher.publishAdvertisementUpdated(
                    new AdvertisementUpdatedEvent(
                            updated.getId(),
                            Instant.now()
                    )
            );

            AdvertisementResponse resp = mapper.toResponse(updated);
            cacheService.save(keyById(id), resp);

            return resp;
        });
    }


    // Upload thumbnail
    @Transactional
    public Optional<AdvertisementResponse> uploadThumbnail(UUID id, MultipartFile file, UUID currentUserId) {
        return repository.findById(id).map(ad -> {
            // здесь можно ещё проверить, что currentUserId == ad.getUserId()

            try {
                // 1. Удалить старую картинку, если есть
                String oldThumbnailUrl = ad.getThumbnailUrl();
                if (oldThumbnailUrl != null && !oldThumbnailUrl.isBlank()) {
                    fileStorageService.deleteByUrl(oldThumbnailUrl);
                }

                // 2. Загрузить новую
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
            // удалить файл, если есть
            String thumbnailUrl = ad.getThumbnailUrl();
            if (thumbnailUrl != null && !thumbnailUrl.isBlank()) {
                fileStorageService.deleteByUrl(thumbnailUrl);
                ad.setThumbnailUrl(null);
            }

            ad.setStatus(AdvertisementStatus.DELETED);
            repository.save(ad);

            advertisementEventPublisher.publishAdvertisementDeleted(
                    new AdvertisementDeletedEvent(
                            ad.getId(),
                            Instant.now()
                    )
            );

            cacheService.delete(keyById(id));
            return true;
        }).orElse(false);
    }
}
