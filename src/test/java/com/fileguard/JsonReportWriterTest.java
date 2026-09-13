
package com.fileguard;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonReportWriterTest {

    @Test
    void shouldWriteJsonReport() throws Exception {

        Path outputFile =
                Files.createTempFile(
                        "fileguard-report",
                        ".json"
                );

        FileChange change =
                new FileChange(
                        Path.of("/tmp/config.txt"),
                        ChangeType.MODIFIED,
                        "old-hash",
                        "new-hash"
                );

        VerificationReport report =
                new VerificationReport(
                        2,
                        1,
                        0,
                        0,
                        true,
                        List.of(change)
                );

        JsonReportWriter writer =
                new JsonReportWriter();

        writer.write(
                report,
                outputFile
        );

        String content =
                Files.readString(outputFile);

        assertTrue(
                content.contains(
                        "\"modified\" : 1"
                )
        );

        assertTrue(
                content.contains(
                        "\"compromised\" : true"
                )
        );

        assertTrue(
                content.contains(
                        "\"old-hash\""
                )
        );

        assertTrue(
                content.contains(
                        "\"new-hash\""
                )
        );
    }

    @Test
    void shouldWriteEmptyChangesList() throws Exception {

        Path outputFile =
                Files.createTempFile(
                        "fileguard-report",
                        ".json"
                );

        VerificationReport report =
                new VerificationReport(
                        0,
                        0,
                        0,
                        0,
                        false,
                        List.of()
                );

        JsonReportWriter writer =
                new JsonReportWriter();

        writer.write(
                report,
                outputFile
        );

        String content =
                Files.readString(outputFile);

        assertFalse(
                content.isEmpty()
        );

        assertTrue(
                content.contains(
                        "\"changes\" : [ ]"
                )
                || content.contains(
                        "\"changes\" : []"
                )
        );
    }
}

