package com.example.demo.aggregators;

import biweekly.ICalendar;
import biweekly.component.VEvent;
import com.example.demo.ChannelsConfig;
import com.example.demo.ICalGroupProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@Import({ICalGroupProcessor.class, MonthlyAggregationFlow.class, ChannelsConfig.class})
@EnableIntegration
class MonthlyAggregationFlowTest {

    @Autowired
    private MessageChannel vEventsChannel;

    @Autowired
    private DirectChannel iCalChannel;

    @Mock
    MessageHandler mockMessageHandler;

    @Captor
    private ArgumentCaptor<Message<?>> captor;

    @Test
    void test() {
        // given
        iCalChannel.subscribe(mockMessageHandler);

        // when
        vEventsChannel.send(new GenericMessage<>(anEvent(Instant.parse("2020-10-14T18:55:00Z"))));
        vEventsChannel.send(new GenericMessage<>(anEvent(Instant.parse("2020-10-10T18:55:00Z"))));
        vEventsChannel.send(new GenericMessage<>(anEvent(Instant.parse("2019-11-10T18:55:00Z"))));

        // then
        verify(mockMessageHandler, timeout(6000).times(2)).handleMessage(captor.capture());
        List<Message<?>> messages = captor.getAllValues();
        assertThat(messages, is(not(nullValue())));
        assertThat(messages, hasSize(2));

        for (Message<?> message : messages) {
            if (message.getPayload() instanceof ICalendar iCalendar) {
                assertThat(iCalendar.getEvents(), either(hasSize(2)).or(hasSize(1)));
            } else {
                fail();
            }
        }

        List<VEvent> events = messages.stream()
                .flatMap(m -> ((ICalendar) m.getPayload()).getEvents().stream())
                .collect(Collectors.toList());
        assertThat(events, hasSize(3));
    }

    private VEvent anEvent(Instant time) {
        VEvent vEvent = new VEvent();
        vEvent.setDateStart(Date.from(time));
        return vEvent;
    }
}