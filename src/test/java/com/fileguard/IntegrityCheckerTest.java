package com.fileguard;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IntegrityCheckerTest {

    private final IntegrityChecker checker = new IntegrityChecker();

    @Test
    void shouldDetectUnchangedFile() {
        Path file = Path.of("/tmp/config.txt");

        Map<Path, String> baseline = Map.of(
                file, "abc123"
        );

        Map<Path, String> current = Map.of(
                file, "abc123"
        );

        List<FileChange> changes =
                checker.compare(baseline, current);

        assertEquals(1, changes.size());
        assertEquals(ChangeType.UNCHANGED, changes.get(0).type());
    }

    @Test
    void shouldDetectModifiedFile() {
        Path file = Path.of("/tmp/config.txt");

        Map<Path, String> baseline = Map.of(
                file, "abc123"
        );

        Map<Path, String> current = Map.of(
                file, "changed456"
        );

        List<FileChange> changes =
                checker.compare(baseline, current);

        assertEquals(1, changes.size());
        assertEquals(ChangeType.MODIFIED, changes.get(0).type());
    }

    @Test
    void shouldDetectNewFile() {
        Path file = Path.of("/tmp/new.txt");

        Map<Path, String> baseline = Map.of();

        Map<Path, String> current = Map.of(
                file, "new123"
        );

        List<FileChange> changes =
                checker.compare(baseline, current);

        assertEquals(1, changes.size());
        assertEquals(ChangeType.NEW, changes.get(0).type());
    }

    @Test
    void shouldDetectDeletedFile() {
        Path file = Path.of("/tmp/deleted.txt");

        Map<Path, String> baseline = Map.of(
                file, "abc123"
        );

        Map<Path, String> current = Map.of();

        List<FileChange> changes =
                checker.compare(baseline, current);

        assertEquals(1, changes.size());
        assertEquals(ChangeType.DELETED, changes.get(0).type());
    }
}
