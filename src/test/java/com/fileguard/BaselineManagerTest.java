package com.fileguard;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BaselineManagerTest {

    @Test
    void shouldSaveAndLoadBaseline() throws Exception {
        Path baseline = Files.createTempFile(
                "fileguard-baseline",
                ".txt"
        );

        Map<Path, String> original = new LinkedHashMap<>();

        original.put(
                Path.of("/tmp/file1.txt"),
                "abc123"
        );

        original.put(
                Path.of("/tmp/file2.txt"),
                "def456"
        );

        BaselineManager manager = new BaselineManager();

        manager.save(original, baseline);

        Map<Path, String> loaded =
                manager.load(baseline);

        assertEquals(original, loaded);

        Files.deleteIfExists(baseline);
    }

    @Test
    void loadThrowsExceptionForMissingBaseline() {
        BaselineManager manager = new BaselineManager();

        Path missingBaseline =
                Path.of("does-not-exist-baseline");

        Exception exception = assertThrows(
                java.io.IOException.class,
                () -> manager.load(missingBaseline)
        );

        assertEquals(
                "Baseline file does not exist: does-not-exist-baseline",
                exception.getMessage()
        );
    }

    @Test
    void shouldSaveBaselineToCustomPath() throws Exception {
        Path customBaseline = Files.createTempFile(
                "custom-baseline",
                ".txt"
        );

        Map<Path, String> original = new LinkedHashMap<>();

        original.put(
                Path.of("/tmp/file.txt"),
                "abc123"
        );

        BaselineManager manager = new BaselineManager();

        manager.save(
                original,
                customBaseline
        );

        assertTrue(
                Files.exists(customBaseline)
        );

        Map<Path, String> loaded =
                manager.load(customBaseline);

        assertEquals(
                original,
                loaded
        );

        Files.deleteIfExists(customBaseline);
    }
}