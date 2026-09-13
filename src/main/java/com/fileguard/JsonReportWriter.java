package com.fileguard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Path;

public class JsonReportWriter {

    private final ObjectMapper objectMapper;

    public JsonReportWriter() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(
                SerializationFeature.INDENT_OUTPUT
        );
    }

    public void write(
            VerificationReport report,
            Path outputFile)
            throws IOException {

        objectMapper.writeValue(
                outputFile.toFile(),
                report
        );
    }
}