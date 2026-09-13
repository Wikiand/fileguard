package com.fileguard;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileHasherTest {

    @Test
    void shouldGenerateSha256Hash() throws Exception {
        Path file = Files.createTempFile("fileguard-test", ".txt");

        Files.writeString(file, "hello");

        FileHasher hasher = new FileHasher();

        String hash = hasher.hash(file);

        assertEquals(
                "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824",
                hash
        );

        Files.deleteIfExists(file);
    }
}
