package AdvertisementService.event;

import AdvertisementService.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdvertisementEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishAdvertisementCreated(AdvertisementCreatedEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "", event);
    }

    public void publishAdvertisementUpdated(AdvertisementUpdatedEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "", event);
    }

    public void publishAdvertisementDeleted(AdvertisementDeletedEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "", event);
    }
}
