package com.fileguard;

import java.nio.file.Path;

public record FileChange(
        Path path,
        ChangeType type
) {
}
