package AdvertisementService.repository;

import AdvertisementService.model.Advertisement;
import AdvertisementService.model.AdvertisementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AdvertisementRepository extends JpaRepository<Advertisement, UUID> {
    List<Advertisement> findAllByUserIdAndStatus(UUID userId, AdvertisementStatus status);
}