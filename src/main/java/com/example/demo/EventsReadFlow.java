package com.example.demo;

import biweekly.component.VEvent;
import biweekly.property.Summary;
import biweekly.util.Duration;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Transformers;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static java.time.Duration.ofSeconds;
import static org.springframework.integration.dsl.Pollers.fixedDelay;

@Slf4j
@Configuration
public class EventsReadFlow {

    @Autowired
    @Qualifier("vEventsChannel")
    private MessageChannel vEventsChannel;

    @Value("${input-folder:.}")
    private String inputFolder;

    @Bean
    public IntegrationFlow readFlow(VEventTransformer eventTransformer) {
        ResolvableType listOfJsonElement = ResolvableType.forClassWithGenerics(List.class, JsonElement.class);

        Path inputDir = Paths.get(inputFolder);
        log.info("Input directory {}", inputDir.toAbsolutePath());

        return IntegrationFlows
                .from(Files.inboundAdapter(inputDir.toFile()).regexFilter(".*\\.json$"),
                        e -> e.poller(fixedDelay(ofSeconds(1)).get()))
                .log(m -> "Processing " + m.getHeaders().get("file_name"))
                .transform(Transformers.fromJson(listOfJsonElement))
                .split()
                .transform(eventTransformer)
                .channel(vEventsChannel)
                .get();
    }

    @Data
    public static class JsonElement {
        String title;
        String titleUrl;
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant time;
    }

    @Component
    private static class VEventTransformer implements GenericTransformer<JsonElement, VEvent> {
        @Override
        public VEvent transform(JsonElement e) {
            VEvent event = new VEvent();
            Summary summary = event.setSummary(e.getTitle());
            summary.setLanguage(Locale.getDefault().toString());
            event.setDescription(e.getTitleUrl());
            event.setDateStart(Date.from(e.getTime()));
            event.setDuration(Duration.builder().minutes(5).build());
            event.setUid(DateTimeFormatter.ISO_INSTANT.format(e.getTime()) + "_" + e.getTitleUrl());
            return event;
        }
    }
}
