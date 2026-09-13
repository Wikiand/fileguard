package com.fileguard;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FileMonitor {

    private final FileScanner scanner;
    private final IntegrityChecker checker;
    private final MonitorLogger logger;

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    public FileMonitor(
            FileScanner scanner,
            IntegrityChecker checker) {

        this.scanner = scanner;
        this.checker = checker;
        this.logger =
                new MonitorLogger(
                        Path.of("fileguard-monitor.log")
                );
    }

    public List<FileChange> check(
            Path directory,
            Map<Path, String> baseline)
            throws FileGuardException {

        Map<Path, String> current =
                scanner.scan(directory);

        return checker.compare(
                baseline,
                current
        );
    }

    public void monitor(
            Path directory,
            Map<Path, String> baseline,
            int intervalSeconds)
            throws FileGuardException {

        System.out.println("FileGuard Monitor");
        System.out.println("-----------------");
        System.out.println("Monitoring: " + directory);
        System.out.println(
                "Interval: " + intervalSeconds + " seconds"
        );
        System.out.println();

        logEvent("MONITOR_STARTED");

        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> logEvent("MONITOR_STOPPED"))
        );

        Set<String> previouslyReported =
                new HashSet<>();

        while (true) {

            List<FileChange> changes =
                    check(directory, baseline);

            Set<String> currentChanges =
                    new HashSet<>();

            for (FileChange change : changes) {

                if (change.type() == ChangeType.UNCHANGED) {
                    continue;
                }

                String eventKey =
                        change.type() + ":" + change.path();

                currentChanges.add(eventKey);

                if (!previouslyReported.contains(eventKey)) {

                    String timestamp =
                            LocalTime.now()
                                    .format(TIME_FORMAT);

                    String message =
                            "⚠ " + change.type() + ": "
                                    + change.path();

                    System.out.println(
                            "[" + timestamp + "] "
                                    + message
                    );

                    logEvent(message);
                }
            }

            boolean compromised =
                    !currentChanges.isEmpty();

            boolean previouslyCompromised =
                    !previouslyReported.isEmpty();

            if (compromised != previouslyCompromised) {

                String timestamp =
                        LocalTime.now()
                                .format(TIME_FORMAT);

                String statusMessage;

                if (compromised) {
                    statusMessage =
                            "Integrity status: COMPROMISED";
                } else {
                    statusMessage =
                            "Integrity status: OK";
                }

                System.out.println(
                        "[" + timestamp + "] "
                                + statusMessage
                );

                logEvent(statusMessage);

                System.out.println();
            }

            previouslyReported = currentChanges;

            try {
                Thread.sleep(
                        intervalSeconds * 1000L
                );

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

                System.out.println(
                        "Monitoring stopped."
                );

                return;
            }
        }
    }

    private void logEvent(String message) {

        try {
            logger.log(message);
        } catch (IOException e) {
            System.err.println(
                    "Warning: Could not write "
                            + "monitor log: "
                            + e.getMessage()
            );
        }
    }
}