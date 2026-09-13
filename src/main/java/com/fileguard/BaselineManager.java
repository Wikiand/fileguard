package com.fileguard;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class BaselineManager {

    public void save(Map<Path, String> hashes, Path baselineFile)
            throws IOException {

        StringBuilder content = new StringBuilder();

        for (Map.Entry<Path, String> entry : hashes.entrySet()) {
            content.append(entry.getKey().toAbsolutePath())
                    .append("|")
                    .append(entry.getValue())
                    .append(System.lineSeparator());
        }

        Files.writeString(baselineFile, content.toString());
    }

    public Map<Path, String> load(Path baselineFile) throws IOException {

        if (!Files.exists(baselineFile)) {
            throw new IOException(
                    "Baseline file does not exist: " + baselineFile
            );
        }

        Map<Path, String> hashes = new LinkedHashMap<>();

        for (String line : Files.readAllLines(baselineFile)) {

            if (line.trim().isEmpty()) {
                continue;
            }

            String[] parts = line.split("\\|", 2);

            if (parts.length != 2) {
                throw new IOException(
                        "Invalid baseline entry: " + line
                );
            }

            hashes.put(
                    Path.of(parts[0]),
                    parts[1]
            );
        }

        return hashes;
    }
}