package org.example.crm.application.port.out;


import java.time.OffsetDateTime;

public interface Clock {
    OffsetDateTime now();
}
