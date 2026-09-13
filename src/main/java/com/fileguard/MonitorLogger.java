package com.fileguard;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MonitorLogger {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Path logFile;

    public MonitorLogger(Path logFile) {
        this.logFile = logFile;
    }

    public void log(String message) throws IOException {

        String timestamp =
                LocalDateTime.now()
                        .format(TIME_FORMAT);

        String entry =
                "[" + timestamp + "] " + message
                        + System.lineSeparator();

        Files.writeString(
                logFile,
                entry,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );
    }
}