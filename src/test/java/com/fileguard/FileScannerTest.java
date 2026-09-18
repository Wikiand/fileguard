package com.fileguard;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileScannerTest {

    @Test
    void scanFindsRegularFiles() throws Exception {
        Path directory = Files.createTempDirectory("fileguard-test");

        Files.writeString(
                directory.resolve("first.txt"),
                "first"
        );

        Files.writeString(
                directory.resolve("second.txt"),
                "second"
        );

        FileHasher hasher = new FileHasher();
        FileScanner scanner = new FileScanner(hasher);

        Map<Path, String> result = scanner.scan(directory);

        assertEquals(2, result.size());
        assertTrue(
                result.containsKey(
                        directory.resolve("first.txt")
                                .toAbsolutePath()
                                .normalize()
                )
        );
        assertTrue(
                result.containsKey(
                        directory.resolve("second.txt")
                                .toAbsolutePath()
                                .normalize()
                )
        );
    }

    @Test
    void scanIgnoresFileGuardGeneratedFiles() throws Exception {
        Path directory = Files.createTempDirectory("fileguard-test");

        Files.writeString(
                directory.resolve("normal.txt"),
                "normal"
        );

        Files.writeString(
                directory.resolve(".fileguard-baseline.txt"),
                "baseline"
        );

        Files.writeString(
                directory.resolve("fileguard-report.json"),
                "{}"
        );

        FileHasher hasher = new FileHasher();
        FileScanner scanner = new FileScanner(hasher);

        Map<Path, String> result = scanner.scan(directory);

        assertEquals(1, result.size());

        Path normalFile =
                directory.resolve("normal.txt")
                        .toAbsolutePath()
                        .normalize();

        Path baselineFile =
                directory.resolve(".fileguard-baseline.txt")
                        .toAbsolutePath()
                        .normalize();

        Path reportFile =
                directory.resolve("fileguard-report.json")
                        .toAbsolutePath()
                        .normalize();

        assertTrue(result.containsKey(normalFile));
        assertFalse(result.containsKey(baselineFile));
        assertFalse(result.containsKey(reportFile));
    }

    @Test
    void scanThrowsExceptionForMissingDirectory() {
        FileHasher hasher = new FileHasher();
        FileScanner scanner = new FileScanner(hasher);

        Path missingDirectory = Path.of("does-not-exist");

        FileGuardException exception = assertThrows(
                FileGuardException.class,
                () -> scanner.scan(missingDirectory)
        );

        assertEquals(
                "Directory does not exist or is not readable: does-not-exist",
                exception.getMessage()
        );
    }
}