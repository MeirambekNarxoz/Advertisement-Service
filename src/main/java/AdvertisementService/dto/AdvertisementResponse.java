package AdvertisementService.dto;

import AdvertisementService.model.AdvertisementStatus;

import java.util.UUID;

public class AdvertisementResponse {
   private UUID id;
   private String title;
   private String description;
   private Integer price;
   private String thumbnailUrl;
   private AdvertisementStatus status;
}