    package AdvertisementService.dto;

    import lombok.AllArgsConstructor;
    import lombok.Getter;
    import lombok.NoArgsConstructor;
    import lombok.Setter;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public class AdvertisementRequest {
        private String title;
        private String description;
        private Integer price;
        private String thumbnailUrl;
    }
