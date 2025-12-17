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

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdvertisementService {

    private final AdvertisementRepository repository;
    private final AdvertisementMapper mapper;
    private final ApplicationEventPublisher eventPublisher; // Добавлено

    // Create
    @Transactional
    public AdvertisementResponse createAdvertisement(AdvertisementRequest request, UUID userId) {
        Advertisement ad = mapper.toEntity(request);
        ad.setUserId(userId);
        ad.setStatus(AdvertisementStatus.ACTIVE);

        Advertisement saved = repository.save(ad);

        // Публикация события после сохранения
        eventPublisher.publishEvent(new AdvertisementCreatedEvent(
                saved.getId(),
                saved.getUserId(),
                Instant.now()
        ));

        return mapper.toResponse(saved);
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
        return repository.findById(id).map(mapper::toResponse);
    }

    // Update
    @Transactional
    public Optional<AdvertisementResponse> updateAdvertisement(UUID id, AdvertisementRequest request) {
        return repository.findById(id).map(ad -> {
            ad.setTitle(request.getTitle());
            ad.setDescription(request.getDescription());
            ad.setPrice(request.getPrice());
            ad.setThumbnailUrl(request.getThumbnailUrl());

            Advertisement updated = repository.save(ad);

            // Публикация события после обновления
            eventPublisher.publishEvent(new AdvertisementUpdatedEvent(
                    updated.getId(),
                    Instant.now()
            ));

            return mapper.toResponse(updated);
        });
    }

    // Delete (Soft Delete)
    @Transactional
    public boolean deleteAdvertisement(UUID id) {
        Optional<Advertisement> adOpt = repository.findById(id);

        if (adOpt.isPresent()) {
            Advertisement ad = adOpt.get();

            // Soft delete - меняем статус на DELETED
            ad.setStatus(AdvertisementStatus.DELETED);
            repository.save(ad);

            // Публикация события после удаления
            eventPublisher.publishEvent(new AdvertisementDeletedEvent(
                    ad.getId(),
                    Instant.now()
            ));

            return true;
        }
        return false;
    }
}
