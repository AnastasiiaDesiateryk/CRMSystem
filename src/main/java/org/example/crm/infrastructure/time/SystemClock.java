package org.example.crm.infrastructure.time;

import org.example.crm.application.port.out.Clock;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/**
 * Infrastructure adapter:
 * system time source.
 */
@Component
public class SystemClock implements Clock {

    @Override
    public OffsetDateTime now() {
        return OffsetDateTime.now();
    }
}
