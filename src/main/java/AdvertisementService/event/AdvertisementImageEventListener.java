package AdvertisementService.event;

import AdvertisementService.repository.AdvertisementRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdvertisementImageEventListener {

    private final AdvertisementRepository repository;

    @Transactional
    @RabbitListener(queues = "links")
    public void handleImageShortened(
            LinkCreatedEvent event
    ) {
        repository.findById(event.getAdvertisementId())
                .ifPresent(ad -> {
                    ad.setShortUrl(event.getShortCode());
                    repository.save(ad);
                });
    }
}
