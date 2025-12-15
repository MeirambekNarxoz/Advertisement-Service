package AdvertisementService.service;

import AdvertisementService.dto.AdvertisementRequest;
import AdvertisementService.dto.AdvertisementResponse;
import AdvertisementService.mapper.AdvertisementMapper;
import AdvertisementService.model.Advertisement;
import AdvertisementService.model.AdvertisementStatus;
import AdvertisementService.repository.AdvertisementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdvertisementService {

    private final AdvertisementRepository repository;
    private final AdvertisementMapper mapper;

    // Create
    public AdvertisementResponse createAdvertisement(AdvertisementRequest request, UUID userId) {
        Advertisement ad = mapper.toEntity(request);
        ad.setUserId(userId);
        ad.setStatus(AdvertisementStatus.ACTIVE);

        Advertisement saved = repository.save(ad);
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
    public Optional<AdvertisementResponse> updateAdvertisement(UUID id, AdvertisementRequest request) {
        return repository.findById(id).map(ad -> {
            ad.setTitle(request.getTitle());
            ad.setDescription(request.getDescription());
            ad.setPrice(request.getPrice());
            ad.setThumbnailUrl(request.getThumbnailUrl());
            return mapper.toResponse(repository.save(ad));
        });
    }

    // Delete
    public boolean deleteAdvertisement(UUID id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}
