package com.fileguard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileMonitorTest {

    private Path tempDirectory;
    private FileScanner scanner;
    private FileMonitor monitor;

    @BeforeEach
    void setUp() throws Exception {

        tempDirectory =
                Files.createTempDirectory("fileguard-test");

        scanner = new FileScanner(
                new FileHasher()
        );

        IntegrityChecker checker =
                new IntegrityChecker();

        monitor =
                new FileMonitor(
                        scanner,
                        checker
                );
    }

    @Test
    void shouldDetectModifiedFile()
            throws Exception {

        Path file =
                tempDirectory.resolve("test.txt");

        Files.writeString(
                file,
                "original"
        );

        Map<Path, String> baseline =
                scanner.scan(tempDirectory);

        Files.writeString(
                file,
                "modified"
        );

        var changes =
                monitor.check(
                        tempDirectory,
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
    void shouldDetectRestoredFileAsUnchanged()
            throws Exception {

        Path file =
                tempDirectory.resolve("test.txt");

        Files.writeString(
                file,
                "original"
        );

        Map<Path, String> baseline =
                scanner.scan(tempDirectory);

        Files.writeString(
                file,
                "modified"
        );

        Files.writeString(
                file,
                "original"
        );

        var changes =
                monitor.check(
                        tempDirectory,
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
    void shouldDetectNewFile()
            throws Exception {

        Path file =
                tempDirectory.resolve("test.txt");

        Files.writeString(
                file,
                "original"
        );

        Map<Path, String> baseline =
                scanner.scan(tempDirectory);

        Path newFile =
                tempDirectory.resolve("new.txt");

        Files.writeString(
                newFile,
                "new content"
        );

        var changes =
                monitor.check(
                        tempDirectory,
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
    void shouldDetectDeletedFile()
            throws Exception {

        Path file =
                tempDirectory.resolve("test.txt");

        Files.writeString(
                file,
                "original"
        );

        Map<Path, String> baseline =
                scanner.scan(tempDirectory);

        Files.delete(file);

        var changes =
                monitor.check(
                        tempDirectory,
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

    @Test
    void shouldGenerateVerificationReportForModifiedFile()
            throws Exception {

        Path file =
                tempDirectory.resolve("test.txt");

        Files.writeString(
                file,
                "original"
        );

        Map<Path, String> baseline =
                scanner.scan(tempDirectory);

        Files.writeString(
                file,
                "modified"
        );

        VerificationReport report =
                monitor.verify(
                        tempDirectory,
                        baseline
                );

        assertEquals(
                0,
                report.unchanged()
        );

        assertEquals(
                1,
                report.modified()
        );

        assertEquals(
                0,
                report.newFiles()
        );

        assertEquals(
                0,
                report.deleted()
        );

        assertTrue(
                report.compromised()
        );

        assertEquals(
                1,
                report.changes().size()
        );
    }

    @Test
    void shouldGenerateCleanVerificationReport()
            throws Exception {

        Path file =
                tempDirectory.resolve("test.txt");

        Files.writeString(
                file,
                "original"
        );

        Map<Path, String> baseline =
                scanner.scan(tempDirectory);

        VerificationReport report =
                monitor.verify(
                        tempDirectory,
                        baseline
                );

        assertEquals(
                1,
                report.unchanged()
        );

        assertEquals(
                0,
                report.modified()
        );

        assertEquals(
                0,
                report.newFiles()
        );

        assertEquals(
                0,
                report.deleted()
        );

        assertTrue(
                !report.compromised()
        );

        assertEquals(
                1,
                report.changes().size()
        );
    }
}