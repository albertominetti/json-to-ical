package com.example.demo;

import biweekly.ICalendar;
import biweekly.component.VEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.aggregator.MessageGroupProcessor;
import org.springframework.integration.store.MessageGroup;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ICalGroupProcessor implements MessageGroupProcessor {

    @Override
    public ICalendar processMessageGroup(MessageGroup group) {
        ICalendar ical = new ICalendar();
        for (Message<?> message : group.getMessages()) {
            if (message.getPayload() instanceof VEvent vEvent) {
                ical.addEvent(vEvent);
            } else {
                log.error("Unexpected message instead of VEvent: {}", message);
            }
        }
        return ical;
    }
}
