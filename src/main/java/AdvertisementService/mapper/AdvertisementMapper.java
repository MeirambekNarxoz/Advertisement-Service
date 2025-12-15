package AdvertisementService.mapper;

import AdvertisementService.dto.AdvertisementRequest;
import AdvertisementService.dto.AdvertisementResponse;
import AdvertisementService.model.Advertisement;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AdvertisementMapper {
    AdvertisementResponse toResponse(Advertisement advertisement);
    Advertisement toEntity(AdvertisementRequest request);
}
