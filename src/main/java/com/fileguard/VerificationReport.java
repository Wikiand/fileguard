package com.fileguard;

public record VerificationReport(
        int unchanged,
        int modified,
        int newFiles,
        int deleted,
        boolean compromised
) {
}