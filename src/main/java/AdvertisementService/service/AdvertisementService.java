package AdvertisementService.service;

import AdvertisementService.dto.AdvertisementRequest;
import AdvertisementService.dto.AdvertisementResponse;
import AdvertisementService.mapper.AdvertisementMapper;
import AdvertisementService.model.Advertisement;
import AdvertisementService.model.AdvertisementStatus;
import AdvertisementService.repository.AdvertisementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdvertisementService {

    private final AdvertisementRepository repository;
    private final AdvertisementMapper mapper;
    private final FileStorageService fileStorageService;
    private final CacheService cacheService;

    private String keyById(UUID id) {
        return "ad:" + id;
    }

    // Create
    public AdvertisementResponse createAdvertisement(AdvertisementRequest request, UUID userId) {
        Advertisement ad = mapper.toEntity(request);
        ad.setUserId(userId);
        ad.setStatus(AdvertisementStatus.ACTIVE);

        Advertisement saved = repository.save(ad);
        AdvertisementResponse resp = mapper.toResponse(saved);

        cacheService.save(keyById(saved.getId()), resp);
        cacheService.viewed(saved.getId());
        return resp;
    }

    // Read all (без сложного кэша, можно по желанию добавить)
    public List<AdvertisementResponse> getAllAdvertisements() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    // Read by ID с кэшем + счётчик просмотра
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

    // Update (обновляем и кэш)
    public Optional<AdvertisementResponse> updateAdvertisement(UUID id, AdvertisementRequest request) {
        return repository.findById(id).map(ad -> {
            ad.setTitle(request.getTitle());
            ad.setDescription(request.getDescription());
            ad.setPrice(request.getPrice());
            Advertisement saved = repository.save(ad);
            AdvertisementResponse resp = mapper.toResponse(saved);
            cacheService.save(keyById(id), resp);
            return resp;
        });
    }

    // Загрузка / обновление thumbnail
    public Optional<AdvertisementResponse> uploadThumbnail(UUID id, MultipartFile file, UUID currentUserId) {
        return repository.findById(id).map(ad -> {
            try {
                String urlOrObjectName = fileStorageService.uploadThumbnail(id, file);
                ad.setThumbnailUrl(urlOrObjectName);
                Advertisement saved = repository.save(ad);
                AdvertisementResponse resp = mapper.toResponse(saved);
                cacheService.save(keyById(id), resp);
                return resp;
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload thumbnail", e);
            }
        });
    }

    // Delete
    public boolean deleteAdvertisement(UUID id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            cacheService.delete(keyById(id));
            return true;
        }
        return false;
    }

    // список популярных объявлений
    public List<AdvertisementResponse> popular(int topN) {
        Set<Object> ids = cacheService.popular(topN);
        Set<UUID> uuidSet = ids.stream()
                .filter(Objects::nonNull)
                .map(obj -> UUID.fromString(obj.toString()))
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
