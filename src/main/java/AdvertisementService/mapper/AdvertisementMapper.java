package AdvertisementService.mapper;

import AdvertisementService.dto.AdvertisementRequest;
import AdvertisementService.dto.AdvertisementResponse;
import AdvertisementService.model.Advertisement;
import org.mapstruct.Mapper;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface AdvertisementMapper {

    AdvertisementResponse toResponse(Advertisement advertisement);

    Advertisement toEntity(AdvertisementRequest request);

    default String map(UUID value) {
        return value != null ? value.toString() : null;
    }

    default UUID map(String value) {
        return value != null ? UUID.fromString(value) : null;
    }
}
