package com.fileguard;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        assertEquals(
                true,
                result.containsKey(
                        directory.resolve("first.txt")
                                .toAbsolutePath()
                                .normalize()
                )
        );
        assertEquals(
                true,
                result.containsKey(
                        directory.resolve("second.txt")
                                .toAbsolutePath()
                                .normalize()
                )
        );
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