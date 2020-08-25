package mina.kafka.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mina.kafka.kafka.producer.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/kafka")
public class KafkaController {

    @Autowired
    private KafkaProducer messageProducer;

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducer.class);

    @GetMapping(value = "/publish")
    public void sendMessageToKafkaTopic(@RequestParam("message") String message) {
        messageProducer.sendMessage(message);
        LOGGER.info("message send : {}", message);
    }
}
