package AdvertisementService.dto;

import AdvertisementService.model.AdvertisementStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdvertisementResponse implements Serializable {
   private UUID id;
   private String title;
   private String description;
   private Integer price;
   private String thumbnailUrl;
   private AdvertisementStatus status;
   private String userId;
}