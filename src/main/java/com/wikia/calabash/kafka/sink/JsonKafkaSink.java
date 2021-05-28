package com.wikia.calabash.kafka.sink;


import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

@Slf4j
public class JsonKafkaSink {

    private KafkaProducer<String, String> kafkaProducer;

    public JsonKafkaSink(Properties props) {
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        kafkaProducer = new KafkaProducer<>(props);
    }

    public void send(String topic, String json) {
        try {
            kafkaProducer.send(new ProducerRecord<>(topic, json));
        } catch (Exception e) {
            log.error("kafka sink fail", e);
        }
    }
}
