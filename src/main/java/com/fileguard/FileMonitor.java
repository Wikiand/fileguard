package com.fileguard;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class FileMonitor {

    private final FileScanner scanner;
    private final IntegrityChecker checker;

    public FileMonitor(
            FileScanner scanner,
            IntegrityChecker checker) {

        this.scanner = scanner;
        this.checker = checker;
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

        boolean previouslyCompromised = false;

        while (true) {

            List<FileChange> changes =
                    check(directory, baseline);

            boolean compromised = false;

            for (FileChange change : changes) {

                if (change.type() != ChangeType.UNCHANGED) {
                    compromised = true;

                    if (!previouslyCompromised) {
                        System.out.println(
                                "⚠ " + change.type() + ": "
                                        + change.path()
                        );
                    }
                }
            }

            if (compromised != previouslyCompromised) {

                if (compromised) {
                    System.out.println(
                            "Integrity status: COMPROMISED"
                    );
                } else {
                    System.out.println(
                            "Integrity status: OK"
                    );
                }

                System.out.println();
            }

            previouslyCompromised = compromised;

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
}