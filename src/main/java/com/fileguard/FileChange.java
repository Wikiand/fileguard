package com.fileguard;

import com.fasterxml.jackson.annotation.JsonGetter;

import java.nio.file.Path;

public record FileChange(
        Path path,
        ChangeType type
) {

    @JsonGetter("path")
    public String pathAsString() {
        return path.toAbsolutePath().normalize().toString();
    }
}