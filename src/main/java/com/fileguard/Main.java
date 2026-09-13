
package com.fileguard;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class Main {

    private static final Path DEFAULT_BASELINE =
            Path.of(".fileguard-baseline");

    public static void main(String[] args) {

        if (args.length == 1 && args[0].equals("--help")) {
            printHelp();
            return;
        }

        if (args.length != 2 && args.length != 4) {
            System.err.println(
                    "Usage: java -jar fileguard.jar <scan|verify> <directory> [--baseline <file>]"
            );
            System.exit(1);
        }

        String command = args[0];
        Path directory = Path.of(args[1]);
        Path baseline = getBaselinePath(args);

        try {
            FileHasher hasher = new FileHasher();
            FileScanner scanner = new FileScanner(hasher);
            BaselineManager baselineManager = new BaselineManager();
            IntegrityChecker checker = new IntegrityChecker();

            switch (command) {

                case "scan" -> scan(
                        directory,
                        scanner,
                        baselineManager,
                        baseline
                );

                case "verify" -> {
                    boolean compromised = verify(
                            directory,
                            scanner,
                            baselineManager,
                            checker,
                            baseline
                    );

                    if (compromised) {
                        System.exit(1);
                    }
                }

                default -> {
                    System.err.println(
                            "Unknown command: " + command
                    );
                    System.err.println(
                            "Use 'scan' or 'verify'."
                    );
                    System.exit(1);
                }
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static Path getBaselinePath(String[] args) {

        if (args.length == 2) {
            return DEFAULT_BASELINE;
        }

        if (!args[2].equals("--baseline")) {
            System.err.println(
                    "Unknown option: " + args[2]
            );
            System.exit(1);
        }

        if (args[3].isBlank()) {
            System.err.println(
                    "Baseline file path cannot be empty."
            );
            System.exit(1);
        }

        return Path.of(args[3]);
    }

    private static void printHelp() {

        System.out.println(
                "Usage: java -jar fileguard.jar <scan|verify> <directory> [--baseline <file>]"
        );

        System.out.println();

        System.out.println("Commands:");

        System.out.println(
                "  scan    Create a baseline of the directory"
        );

        System.out.println(
                "  verify  Check the directory against the baseline"
        );

        System.out.println();

        System.out.println("Options:");

        System.out.println(
                "  --baseline <file>    Specify a custom baseline file"
        );

        System.out.println();

        System.out.println("Examples:");

        System.out.println(
                "  java -jar fileguard.jar scan test-files"
        );

        System.out.println(
                "  java -jar fileguard.jar verify test-files"
        );

        System.out.println(
                "  java -jar fileguard.jar scan test-files --baseline baseline.db"
        );

        System.out.println(
                "  java -jar fileguard.jar verify test-files --baseline baseline.db"
        );
    }

    private static void scan(
            Path directory,
            FileScanner scanner,
            BaselineManager baselineManager,
            Path baseline)
            throws Exception {

        Map<Path, String> hashes =
                scanner.scan(directory);

        baselineManager.save(
                hashes,
                baseline
        );

        System.out.println("FileGuard Scan");
        System.out.println("--------------");
        System.out.println("Directory: " + directory);
        System.out.println("Files scanned: " + hashes.size());
        System.out.println(
                "Baseline saved: "
                        + baseline.toAbsolutePath()
        );
    }

    private static boolean verify(
            Path directory,
            FileScanner scanner,
            BaselineManager baselineManager,
            IntegrityChecker checker,
            Path baseline)
            throws Exception {

        Map<Path, String> baselineHashes =
                baselineManager.load(baseline);

        Map<Path, String> current =
                scanner.scan(directory);

        List<FileChange> changes =
                checker.compare(
                        baselineHashes,
                        current
                );

        System.out.println("FileGuard Verification");
        System.out.println("----------------------");

        boolean compromised = false;

        int unchanged = 0;
        int modified = 0;
        int newFiles = 0;
        int deleted = 0;

        for (FileChange change : changes) {

            switch (change.type()) {

                case UNCHANGED -> {
                    System.out.println(
                            "✓ UNCHANGED: " + change.path()
                    );
                    unchanged++;
                }

                case MODIFIED -> {
                    System.out.println(
                            "⚠ MODIFIED: " + change.path()
                    );
                    modified++;
                    compromised = true;
                }

                case NEW -> {
                    System.out.println(
                            "⚠ NEW: " + change.path()
                    );
                    newFiles++;
                    compromised = true;
                }

                case DELETED -> {
                    System.out.println(
                            "⚠ DELETED: " + change.path()
                    );
                    deleted++;
                    compromised = true;
                }
            }
        }

        System.out.println();

        System.out.println("Verification Summary");
        System.out.println("--------------------");
        System.out.println("Unchanged: " + unchanged);
        System.out.println("Modified:  " + modified);
        System.out.println("New:       " + newFiles);
        System.out.println("Deleted:   " + deleted);

        System.out.println();

        if (compromised) {
            System.out.println(
                    "Integrity status: COMPROMISED"
            );
        } else {
            System.out.println(
                    "Integrity status: OK"
            );
        }

        return compromised;
    }
}