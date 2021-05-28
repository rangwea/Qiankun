package com.wikia.calabash.kafka.sink;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * @author wikia
 * @since 2019/12/21 14:44
 */
//@Configuration
public class KafkaSinkConfiguration {

    @Value("${sink.xxx.kafka.topic}")
    private String topic;

    @Bean("kafkaSink")
    public JsonKafkaSink druidKafkaSink(@Qualifier("kafkaProperties") Properties properties) {
        return new JsonKafkaSink(properties);
    }

    @Bean("kafkaProperties")
    @ConfigurationProperties(prefix = "sink.xxx.kafka")
    public Properties druidKafkaProperties() {
        return new Properties();
    }
}
