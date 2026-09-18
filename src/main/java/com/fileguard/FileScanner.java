package com.fileguard;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

public class FileScanner {

    private static final String BASELINE_FILE_NAME =
            ".fileguard-baseline.txt";

    private static final String REPORT_FILE_NAME =
            "fileguard-report.json";

    private final FileHasher hasher;

    public FileScanner(FileHasher hasher) {
        this.hasher = hasher;
    }

    public Map<Path, String> scan(Path directory) throws FileGuardException {
        Map<Path, String> files = new LinkedHashMap<>();

        if (!Files.isDirectory(directory)) {
            throw new FileGuardException(
                    "Directory does not exist or is not readable: "
                            + directory
            );
        }

        try (Stream<Path> paths = Files.walk(directory)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> !isFileGuardGeneratedFile(path))
                    .forEach(path -> {
                        try {
                            files.put(
                                    path.toAbsolutePath().normalize(),
                                    hasher.hash(path)
                            );
                        } catch (IOException e) {
                            throw new FileScanException(path, e);
                        }
                    });
        } catch (FileScanException e) {
            throw new FileGuardException(
                    "Could not read file '"
                            + e.getPath()
                            + "'",
                    e.getCause()
            );
        } catch (IOException e) {
            throw new FileGuardException(
                    "Could not scan directory '"
                            + directory
                            + "': "
                            + e.getMessage(),
                    e
            );
        }

        return files;
    }

    private boolean isFileGuardGeneratedFile(Path path) {
        String fileName = path.getFileName().toString();

        return fileName.equals(BASELINE_FILE_NAME)
                || fileName.equals(REPORT_FILE_NAME);
    }

    private static class FileScanException extends RuntimeException {

        private final Path path;

        FileScanException(Path path, IOException cause) {
            super(cause);
            this.path = path;
        }

        public Path getPath() {
            return path;
        }
    }
}