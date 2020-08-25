package mina.kafka.kafka.consumer;

import mina.kafka.kafka.producer.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Service
public class KafkaConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducer.class);

    @KafkaListener(topics = "${message.topic.name}", groupId = "${kafka.groupId}")
    public void listen(String message) {
        LOGGER.info("Received Messasge in group : " + message);
    }
}
