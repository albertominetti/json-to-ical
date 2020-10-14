package com.example.demo.aggregators;

import biweekly.component.VEvent;
import com.example.demo.ICalGroupProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static com.example.demo.ChannelsConfig.ICAL_KEY;

@Slf4j
@Configuration
public class YearlyAggregationFlow {
    public static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("yyyy")
            .withLocale(Locale.getDefault())
            .withZone(ZoneId.systemDefault());

    @Autowired
    @Qualifier("vEventsChannel")
    private PublishSubscribeChannel vEventsChannel;

    @Autowired
    @Qualifier("iCalChannel")
    private DirectChannel iCalChannel;

    @Bean
    public IntegrationFlow yearAggr(ICalGroupProcessor groupProcessor) {
        return IntegrationFlows
                .from(vEventsChannel)
                .enrichHeaders(h -> h.<VEvent>headerFunction(ICAL_KEY,
                        m -> YEAR_FORMATTER.format(m.getPayload().getDateStart().getValue().toInstant()))
                )
                .aggregate(a -> a.correlationStrategy(h -> h.getHeaders().get(ICAL_KEY))
                        .groupTimeout(1_000).sendPartialResultOnExpiry(true)
                        .outputProcessor(groupProcessor)
                )
                .channel(iCalChannel)
                .get();
    }

}
