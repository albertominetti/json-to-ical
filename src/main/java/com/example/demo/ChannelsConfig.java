package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.PublishSubscribeChannel;

@Configuration
public class ChannelsConfig {
    public static final String ICAL_KEY = "ical_key";

    @Bean
    public DirectChannel iCalChannel() {
        return new DirectChannel();
    }

    @Bean
    public PublishSubscribeChannel vEventsChannel() {
        return new PublishSubscribeChannel();
    }
}
