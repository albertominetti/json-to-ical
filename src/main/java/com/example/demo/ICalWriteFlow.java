package com.example.demo;

import biweekly.Biweekly;
import biweekly.ICalendar;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.file.dsl.Files;
import org.springframework.messaging.MessageChannel;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.example.demo.ChannelsConfig.ICAL_KEY;
import static org.springframework.integration.file.FileHeaders.FILENAME;
import static org.springframework.integration.file.support.FileExistsMode.FAIL;

@Slf4j
@Configuration
public class ICalWriteFlow {

    @Autowired
    @Qualifier("iCalChannel")
    private MessageChannel iCalChannel;


    @Value("${output-folder:./ical}")
    private String outputFolder;

    @Bean
    public IntegrationFlow writeFlow() {
        Path outputDir = Paths.get(outputFolder);
        log.info("Output directory {}", outputDir.toAbsolutePath());

        return IntegrationFlows
                .from(iCalChannel)
                .<ICalendar>log(m -> "Total events for " + m.getHeaders().get(ICAL_KEY)
                        + " are " + m.getPayload().getEvents().size())
                .<ICalendar, String>transform(ical -> Biweekly.write(ical).go())
                .handle(Files.outboundAdapter(outputDir.toFile())
                        .autoCreateDirectory(true).fileExistsMode(FAIL) // do not overwrite the files
                        .fileNameGenerator(m -> String.format("%s_%s.ical",
                                m.getHeaders().getOrDefault(FILENAME, "file"),
                                m.getHeaders().getOrDefault(ICAL_KEY, "calendar"))
                        )
                )
                .get();
    }
}
