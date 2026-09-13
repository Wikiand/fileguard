package com.fileguard;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonReportWriterTest {

    @Test
    void shouldWriteVerificationReportAsJson() throws Exception {

        Path reportFile = Files.createTempFile(
                "fileguard-report",
                ".json"
        );

        VerificationReport report =
                new VerificationReport(
                        2,
                        1,
                        0,
                        0,
                        true,
                        List.of(
                                new FileChange(
                                        Path.of("/test/config.txt"),
                                        ChangeType.MODIFIED
                                )
                        )
                );

        JsonReportWriter writer =
                new JsonReportWriter();

        writer.write(
                report,
                reportFile
        );

        assertTrue(
                Files.exists(reportFile)
        );

        String json =
                Files.readString(reportFile);

        assertTrue(
                json.contains("\"unchanged\" : 2")
        );

        assertTrue(
                json.contains("\"modified\" : 1")
        );

        assertTrue(
                json.contains("\"newFiles\" : 0")
        );

        assertTrue(
                json.contains("\"deleted\" : 0")
        );

        assertTrue(
                json.contains("\"compromised\" : true")
        );

        assertTrue(
                json.contains("\"changes\"")
        );

        assertTrue(
                json.contains("\"type\" : \"MODIFIED\"")
        );

        Files.deleteIfExists(reportFile);
    }

    @Test
    void shouldWriteCleanVerificationReport() throws Exception {

        Path reportFile = Files.createTempFile(
                "fileguard-clean-report",
                ".json"
        );

        VerificationReport report =
                new VerificationReport(
                        3,
                        0,
                        0,
                        0,
                        false,
                        List.of(
                                new FileChange(
                                        Path.of("/test/config.txt"),
                                        ChangeType.UNCHANGED
                                ),
                                new FileChange(
                                        Path.of("/test/users.txt"),
                                        ChangeType.UNCHANGED
                                ),
                                new FileChange(
                                        Path.of("/test/notes.txt"),
                                        ChangeType.UNCHANGED
                                )
                        )
                );

        JsonReportWriter writer =
                new JsonReportWriter();

        writer.write(
                report,
                reportFile
        );

        String json =
                Files.readString(reportFile);

        assertEquals(
                true,
                json.contains("\"compromised\" : false")
        );

        assertTrue(
                json.contains("\"changes\"")
        );

        assertTrue(
                json.contains("\"type\" : \"UNCHANGED\"")
        );

        Files.deleteIfExists(reportFile);
    }
}