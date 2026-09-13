package com.fileguard;

import java.util.List;

public record VerificationReport(
        int unchanged,
        int modified,
        int newFiles,
        int deleted,
        boolean compromised,
        List<FileChange> changes
) {
}