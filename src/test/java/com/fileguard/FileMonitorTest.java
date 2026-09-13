package com.fileguard;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileMonitorTest {

    @Test
    void shouldDetectModifiedFile() throws Exception {

        Path directory =
                Files.createTempDirectory("fileguard-monitor");

        Path file =
                directory.resolve("config.txt");

        Files.writeString(
                file,
                "original content"
        );

        FileHasher hasher =
                new FileHasher();

        FileScanner scanner =
                new FileScanner(hasher);

        IntegrityChecker checker =
                new IntegrityChecker();

        FileMonitor monitor =
                new FileMonitor(
                        scanner,
                        checker
                );

        Map<Path, String> baseline =
                scanner.scan(directory);

        Files.writeString(
                file,
                "modified content"
        );

        var changes =
                monitor.check(
                        directory,
                        baseline
                );

        assertEquals(
                1,
                changes.stream()
                        .filter(change ->
                                change.type()
                                        == ChangeType.MODIFIED)
                        .count()
        );
    }

    @Test
    void shouldDetectRestoredFileAsUnchanged() throws Exception {

        Path directory =
                Files.createTempDirectory("fileguard-monitor");

        Path file =
                directory.resolve("config.txt");

        String originalContent =
                "original content";

        Files.writeString(
                file,
                originalContent
        );

        FileHasher hasher =
                new FileHasher();

        FileScanner scanner =
                new FileScanner(hasher);

        IntegrityChecker checker =
                new IntegrityChecker();

        FileMonitor monitor =
                new FileMonitor(
                        scanner,
                        checker
                );

        Map<Path, String> baseline =
                scanner.scan(directory);

        Files.writeString(
                file,
                "modified content"
        );

        Files.writeString(
                file,
                originalContent
        );

        var changes =
                monitor.check(
                        directory,
                        baseline
                );

        assertEquals(
                1,
                changes.stream()
                        .filter(change ->
                                change.type()
                                        == ChangeType.UNCHANGED)
                        .count()
        );
    }

    @Test
    void shouldDetectNewFile() throws Exception {

        Path directory =
                Files.createTempDirectory("fileguard-monitor");

        Path originalFile =
                directory.resolve("config.txt");

        Files.writeString(
                originalFile,
                "original content"
        );

        FileHasher hasher =
                new FileHasher();

        FileScanner scanner =
                new FileScanner(hasher);

        IntegrityChecker checker =
                new IntegrityChecker();

        FileMonitor monitor =
                new FileMonitor(
                        scanner,
                        checker
                );

        Map<Path, String> baseline =
                scanner.scan(directory);

        Path newFile =
                directory.resolve("malicious.txt");

        Files.writeString(
                newFile,
                "unexpected file"
        );

        var changes =
                monitor.check(
                        directory,
                        baseline
                );

        assertEquals(
                1,
                changes.stream()
                        .filter(change ->
                                change.type()
                                        == ChangeType.NEW)
                        .count()
        );
    }

    @Test
    void shouldDetectDeletedFile() throws Exception {

        Path directory =
                Files.createTempDirectory("fileguard-monitor");

        Path file =
                directory.resolve("config.txt");

        Files.writeString(
                file,
                "original content"
        );

        FileHasher hasher =
                new FileHasher();

        FileScanner scanner =
                new FileScanner(hasher);

        IntegrityChecker checker =
                new IntegrityChecker();

        FileMonitor monitor =
                new FileMonitor(
                        scanner,
                        checker
                );

        Map<Path, String> baseline =
                scanner.scan(directory);

        Files.delete(file);

        var changes =
                monitor.check(
                        directory,
                        baseline
                );

        assertEquals(
                1,
                changes.stream()
                        .filter(change ->
                                change.type()
                                        == ChangeType.DELETED)
                        .count()
        );
    }
}