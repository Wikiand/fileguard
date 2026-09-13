package com.fileguard;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MonitorLoggerTest {

    @Test
    void shouldWriteLogEntry() throws Exception {

        Path logFile =
                Files.createTempFile(
                        "fileguard-monitor",
                        ".log"
                );

        MonitorLogger logger =
                new MonitorLogger(logFile);

        logger.log(
                "MODIFIED: /tmp/config.txt"
        );

        String content =
                Files.readString(logFile);

        assertTrue(
                content.contains(
                        "MODIFIED: /tmp/config.txt"
                )
        );
    }
}