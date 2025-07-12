package com.jpmc.midascore;

import com.jpmc.midascore.foundation.Transaction;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class Listener {
    // listener to listen for the topic in application.yml file
    @KafkaListener(topics = "${general.kafka-topic}", groupId = "listener")
    // listen for transaction objects
    public void listen(Transaction transaction) {
        System.out.println("Received transaction: " + transaction.toString());
    }
}
