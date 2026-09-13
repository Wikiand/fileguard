package com.fileguard;

import java.nio.file.Path;
import java.util.ArrayList;
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

        if (args.length < 2) {
            printUsageError(
                    "Missing command or directory."
            );
        }

        String command = args[0];

        if (!command.equals("scan")
                && !command.equals("verify")
                && !command.equals("monitor")) {

            printUsageError(
                    "Unknown command: " + command
            );
        }

        Path directory = Path.of(args[1]);

        Path baseline = DEFAULT_BASELINE;
        Path report = null;

        for (int i = 2; i < args.length; i++) {

            String option = args[i];

            switch (option) {

                case "--baseline" -> {

                    if (i + 1 >= args.length) {
                        printUsageError(
                                "--baseline requires a file path."
                        );
                    }

                    if (args[i + 1].startsWith("--")) {
                        printUsageError(
                                "--baseline requires a file path."
                        );
                    }

                    baseline = Path.of(args[++i]);
                }

                case "--report" -> {

                    if (i + 1 >= args.length) {
                        printUsageError(
                                "--report requires a file path."
                        );
                    }

                    if (args[i + 1].startsWith("--")) {
                        printUsageError(
                                "--report requires a file path."
                        );
                    }

                    report = Path.of(args[++i]);
                }

                default -> printUsageError(
                        "Unknown option: " + option
                );
            }
        }

        if (command.equals("scan") && report != null) {
            printUsageError(
                    "--report can only be used with verify."
            );
        }

        try {

            FileHasher hasher =
                    new FileHasher();

            FileScanner scanner =
                    new FileScanner(hasher);

            BaselineManager baselineManager =
                    new BaselineManager();

            IntegrityChecker checker =
                    new IntegrityChecker();

            switch (command) {

                case "scan" -> scan(
                        directory,
                        scanner,
                        baselineManager,
                        baseline
                );

                case "monitor" -> {

                    Map<Path, String> baselineHashes =
                            baselineManager.load(baseline);

                    FileMonitor monitor =
                            new FileMonitor(
                                    scanner,
                                    checker
                            );

                    monitor.monitor(
                            directory,
                            baselineHashes,
                            5
                    );
                }

                case "verify" -> {

                    boolean compromised = verify(
                            directory,
                            scanner,
                            baselineManager,
                            checker,
                            baseline,
                            report
                    );

                    if (compromised) {
                        System.exit(1);
                    }
                }
            }

        } catch (Exception e) {

            System.err.println(
                    "Error: " + e.getMessage()
            );

            System.exit(1);
        }
    }

    private static void printUsageError(
            String message) {

        System.err.println(
                "Error: " + message
        );

        System.err.println();

        System.err.println(
                "Usage: java -jar fileguard.jar " +
                "<scan|verify|monitor> <directory> " +
                "[--baseline <file>] " +
                "[--report <file>]"
        );

        System.exit(1);
    }

    private static void printHelp() {

        System.out.println(
                "Usage: java -jar fileguard.jar " +
                "<scan|verify|monitor> <directory> " +
                "[--baseline <file>] " +
                "[--report <file>]"
        );

        System.out.println();

        System.out.println("Commands:");

        System.out.println(
                "  scan    Create a baseline of the directory"
        );

        System.out.println(
                "  verify  Check the directory against the baseline"
        );

        System.out.println(
                "  monitor Monitor the directory continuously"
        );

        System.out.println();

        System.out.println("Options:");

        System.out.println(
                "  --baseline <file>    Specify a custom baseline file"
        );

        System.out.println(
                "  --report <file>      Write verification results as JSON"
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
                "  java -jar fileguard.jar monitor test-files"
        );

        System.out.println(
                "  java -jar fileguard.jar " +
                "scan test-files --baseline baseline.txt"
        );

        System.out.println(
                "  java -jar fileguard.jar " +
                "verify test-files --baseline baseline.txt"
        );

        System.out.println(
                "  java -jar fileguard.jar " +
                "verify test-files --report report.json"
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

        System.out.println(
                "Directory: " + directory
        );

        System.out.println(
                "Files scanned: " + hashes.size()
        );

        System.out.println(
                "Baseline saved: " +
                baseline.toAbsolutePath()
        );
    }

    private static boolean verify(
            Path directory,
            FileScanner scanner,
            BaselineManager baselineManager,
            IntegrityChecker checker,
            Path baseline,
            Path report)
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

        System.out.println(
                "FileGuard Verification"
        );

        System.out.println(
                "----------------------"
        );

        boolean compromised = false;

        int unchanged = 0;
        int modified = 0;
        int newFiles = 0;
        int deleted = 0;

        for (FileChange change : changes) {

            switch (change.type()) {

                case UNCHANGED -> {
                    System.out.println(
                            "✓ UNCHANGED: " +
                            change.path()
                    );
                    unchanged++;
                }

                case MODIFIED -> {
                    System.out.println(
                            "⚠ MODIFIED: " +
                            change.path()
                    );
                    modified++;
                    compromised = true;
                }

                case NEW -> {
                    System.out.println(
                            "⚠ NEW: " +
                            change.path()
                    );
                    newFiles++;
                    compromised = true;
                }

                case DELETED -> {
                    System.out.println(
                            "⚠ DELETED: " +
                            change.path()
                    );
                    deleted++;
                    compromised = true;
                }
            }
        }

        VerificationReport verificationReport =
                new VerificationReport(
                        unchanged,
                        modified,
                        newFiles,
                        deleted,
                        compromised,
                        new ArrayList<>(changes)
                );

        if (report != null) {

            JsonReportWriter reportWriter =
                    new JsonReportWriter();

            reportWriter.write(
                    verificationReport,
                    report
            );

            System.out.println();

            System.out.println(
                    "JSON report saved: " +
                    report.toAbsolutePath()
            );
        }

        System.out.println();

        System.out.println(
                "Verification Summary"
        );

        System.out.println(
                "--------------------"
        );

        System.out.println(
                "Unchanged: " + unchanged
        );

        System.out.println(
                "Modified:  " + modified
        );

        System.out.println(
                "New:       " + newFiles
        );

        System.out.println(
                "Deleted:   " + deleted
        );

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