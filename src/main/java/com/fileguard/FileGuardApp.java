package com.fileguard;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FileGuardApp extends Application {

    private Label selectedFolderLabel;
    private Label filesLabel;
    private Label modifiedLabel;
    private Label newLabel;
    private Label deletedLabel;
    private Label status;

    private Button startMonitoringButton;
    private Button stopMonitoringButton;

    private VBox securityEventsBox;

    private Path selectedDirectory;
    private Map<Path, String> baseline;

    private volatile boolean monitoring = false;
    private Thread monitoringThread;

    private final FileScanner scanner =
            new FileScanner(new FileHasher());

    private final DateTimeFormatter eventTimeFormat =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public void start(Stage stage) {

        Label title =
                new Label("FileGuard");

        title.setStyle(
                "-fx-font-size: 16px; " +
                "-fx-font-weight: bold;"
        );

        Label subtitle =
                new Label("File Integrity Monitoring");

        subtitle.setStyle(
                "-fx-font-size: 9px;"
        );

        VBox header =
                new VBox(
                        1,
                        title,
                        subtitle
                );

        header.setPadding(
                new Insets(7, 8, 5, 8)
        );

        Label statusTitle =
                new Label("INTEGRITY STATUS");

        statusTitle.setStyle(
                "-fx-font-size: 8px; " +
                "-fx-font-weight: bold;"
        );

        status =
                new Label("✓ PROTECTED");

        status.setStyle(
                "-fx-font-size: 12px; " +
                "-fx-font-weight: bold;"
        );

        VBox statusBox =
                new VBox(
                        2,
                        statusTitle,
                        status
                );

        filesLabel =
                new Label("Files monitored: 0");

        modifiedLabel =
                new Label("Modified: 0");

        newLabel =
                new Label("New: 0");

        deletedLabel =
                new Label("Deleted: 0");

        for (Label label : List.of(
                filesLabel,
                modifiedLabel,
                newLabel,
                deletedLabel
        )) {
            label.setStyle(
                    "-fx-font-size: 9px;"
            );
        }

        VBox statistics =
                new VBox(
                        2,
                        filesLabel,
                        modifiedLabel,
                        newLabel,
                        deletedLabel
                );

        HBox overview =
                new HBox(
                        30,
                        statusBox,
                        statistics
                );

        overview.setAlignment(
                Pos.CENTER_LEFT
        );

        overview.setPadding(
                new Insets(7)
        );

        overview.setStyle(
                "-fx-border-color: #cccccc; " +
                "-fx-border-radius: 4; " +
                "-fx-background-radius: 4;"
        );

        Label folderTitle =
                new Label("Selected Folder");

        folderTitle.setStyle(
                "-fx-font-size: 10px; " +
                "-fx-font-weight: bold;"
        );

        selectedFolderLabel =
                new Label("No folder selected");

        selectedFolderLabel.setStyle(
                "-fx-font-size: 9px;"
        );

        selectedFolderLabel.setWrapText(true);

        VBox folderBox =
                new VBox(
                        3,
                        folderTitle,
                        selectedFolderLabel
                );

        folderBox.setPadding(
                new Insets(7)
        );

        folderBox.setStyle(
                "-fx-border-color: #cccccc; " +
                "-fx-border-radius: 4; " +
                "-fx-background-radius: 4;"
        );

        Label eventsTitle =
                new Label("Recent Security Events");

        eventsTitle.setStyle(
                "-fx-font-size: 10px; " +
                "-fx-font-weight: bold;"
        );

        securityEventsBox =
                new VBox(3);

        addEmptyEventsMessage();

        ScrollPane eventsScrollPane =
                new ScrollPane(
                        securityEventsBox
                );

        eventsScrollPane.setFitToWidth(true);

        eventsScrollPane.setPrefHeight(90);

        eventsScrollPane.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        eventsScrollPane.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED
        );

        VBox eventsBox =
                new VBox(
                        3,
                        eventsTitle,
                        eventsScrollPane
                );

        eventsBox.setPadding(
                new Insets(7)
        );

        eventsBox.setStyle(
                "-fx-border-color: #cccccc; " +
                "-fx-border-radius: 4; " +
                "-fx-background-radius: 4;"
        );

        Button selectFolderButton =
                new Button("Select Folder");

        startMonitoringButton =
                new Button("Start Monitoring");

        stopMonitoringButton =
                new Button("Stop Monitoring");

        selectFolderButton.setPrefWidth(105);
        startMonitoringButton.setPrefWidth(105);
        stopMonitoringButton.setPrefWidth(105);

        stopMonitoringButton.setDisable(true);

        selectFolderButton.setOnAction(event -> {

            if (monitoring) {

                showWarning(
                        "Monitoring is active",
                        "Stop monitoring before selecting another folder."
                );

                return;
            }

            DirectoryChooser chooser =
                    new DirectoryChooser();

            chooser.setTitle(
                    "Select Folder to Monitor"
            );

            File selectedDirectoryFile =
                    chooser.showDialog(stage);

            if (selectedDirectoryFile == null) {
                return;
            }

            selectedDirectory =
                    selectedDirectoryFile.toPath();

            selectedFolderLabel.setText(
                    selectedDirectory
                            .toAbsolutePath()
                            .normalize()
                            .toString()
            );

            try {

                baseline =
                        scanner.scan(selectedDirectory);

                filesLabel.setText(
                        "Files monitored: "
                                + baseline.size()
                );

                modifiedLabel.setText(
                        "Modified: 0"
                );

                newLabel.setText(
                        "New: 0"
                );

                deletedLabel.setText(
                        "Deleted: 0"
                );

                clearSecurityEvents();

                status.setText(
                        "✓ BASELINE READY"
                );

            } catch (FileGuardException e) {

                status.setText(
                        "⚠ SCAN ERROR"
                );

                showError(
                        "Could not scan folder",
                        e.getMessage()
                );
            }
        });

        startMonitoringButton.setOnAction(event -> {

            if (selectedDirectory == null) {

                showWarning(
                        "No folder selected",
                        "Select a folder before starting monitoring."
                );

                return;
            }

            if (baseline == null) {

                showWarning(
                        "No baseline available",
                        "Select a folder first to create a baseline."
                );

                return;
            }

            if (monitoring) {
                return;
            }

            monitoring = true;

            status.setText(
                    "● MONITORING"
            );

            startMonitoringButton.setDisable(true);
            stopMonitoringButton.setDisable(false);
            selectFolderButton.setDisable(true);

            FileMonitor fileMonitor =
                    new FileMonitor(
                            scanner,
                            new IntegrityChecker()
                    );

            monitoringThread =
                    new Thread(() -> {

                        Set<String> previouslyReported =
                                new HashSet<>();

                        while (monitoring) {

                            try {

                                List<FileChange> changes =
                                        fileMonitor.check(
                                                selectedDirectory,
                                                baseline
                                        );

                                int modified = 0;
                                int newFiles = 0;
                                int deleted = 0;

                                Set<String> currentChanges =
                                        new HashSet<>();

                                List<FileChange> newEvents =
                                        new java.util.ArrayList<>();

                                for (FileChange change : changes) {

                                    if (change.type()
                                            == ChangeType.UNCHANGED) {
                                        continue;
                                    }

                                    String eventKey =
                                            change.type()
                                                    + ":"
                                                    + change.path();

                                    currentChanges.add(
                                            eventKey
                                    );

                                    switch (change.type()) {

                                        case MODIFIED:
                                            modified++;
                                            break;

                                        case NEW:
                                            newFiles++;
                                            break;

                                        case DELETED:
                                            deleted++;
                                            break;

                                        case UNCHANGED:
                                            break;
                                    }

                                    if (!previouslyReported
                                            .contains(eventKey)) {

                                        newEvents.add(change);
                                    }
                                }

                                boolean compromised =
                                        !currentChanges.isEmpty();

                                final int modifiedCount =
                                        modified;

                                final int newFileCount =
                                        newFiles;

                                final int deletedCount =
                                        deleted;

                                final boolean integrityCompromised =
                                        compromised;

                                Platform.runLater(() -> {

                                    modifiedLabel.setText(
                                            "Modified: "
                                                    + modifiedCount
                                    );

                                    newLabel.setText(
                                            "New: "
                                                    + newFileCount
                                    );

                                    deletedLabel.setText(
                                            "Deleted: "
                                                    + deletedCount
                                    );

                                    if (integrityCompromised) {

                                        status.setText(
                                                "⚠ COMPROMISED"
                                        );

                                    } else {

                                        status.setText(
                                                "✓ PROTECTED"
                                        );
                                    }
                                });

                                if (!newEvents.isEmpty()) {

                                    Platform.runLater(() -> {

                                        for (FileChange change :
                                                newEvents) {

                                            addSecurityEvent(
                                                    change
                                            );
                                        }
                                    });
                                }

                                previouslyReported =
                                        currentChanges;

                                Thread.sleep(5000);

                            } catch (FileGuardException e) {

                                Platform.runLater(() -> {

                                    status.setText(
                                            "⚠ MONITOR ERROR"
                                    );

                                    monitoring = false;

                                    startMonitoringButton
                                            .setDisable(false);

                                    stopMonitoringButton
                                            .setDisable(true);

                                    selectFolderButton
                                            .setDisable(false);
                                });

                            } catch (InterruptedException e) {

                                Thread.currentThread()
                                        .interrupt();

                                monitoring = false;
                            }
                        }
                    });

            monitoringThread.setDaemon(true);

            monitoringThread.start();
        });

        stopMonitoringButton.setOnAction(event -> {

            stopMonitoring();

            status.setText(
                    "✓ MONITORING STOPPED"
            );
        });

        HBox buttons =
                new HBox(
                        8,
                        selectFolderButton,
                        startMonitoringButton,
                        stopMonitoringButton
                );

        buttons.setAlignment(
                Pos.CENTER
        );

        buttons.setPadding(
                new Insets(5, 0, 2, 0)
        );

        VBox content =
                new VBox(
                        6,
                        overview,
                        folderBox,
                        eventsBox,
                        buttons
                );

        content.setPadding(
                new Insets(4, 7, 7, 7)
        );

        ScrollPane scrollPane =
                new ScrollPane(content);

        scrollPane.setFitToWidth(true);

        scrollPane.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        scrollPane.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED
        );

        BorderPane root =
                new BorderPane();

        root.setTop(header);
        root.setCenter(scrollPane);

        Scene scene =
                new Scene(root, 420, 300);

        stage.setTitle("FileGuard");

        stage.setScene(scene);

        stage.setMinWidth(380);
        stage.setMinHeight(280);

        stage.show();
    }

    private void addSecurityEvent(
            FileChange change) {

        if (securityEventsBox == null) {
            return;
        }

        removeEmptyEventsMessage();

        String timestamp =
                LocalTime.now()
                        .format(eventTimeFormat);

        String fileName =
                change.path()
                        .getFileName()
                        .toString();

        Label eventLabel =
                new Label(
                        timestamp
                                + "  ⚠ "
                                + change.type()
                                + "  "
                                + fileName
                );

        eventLabel.setStyle(
                "-fx-font-size: 9px;"
        );

        eventLabel.setWrapText(true);

        securityEventsBox.getChildren()
                .add(0, eventLabel);
    }

    private void addEmptyEventsMessage() {

        Label emptyEventsLabel =
                new Label(
                        "No security events detected"
                );

        emptyEventsLabel.setStyle(
                "-fx-font-size: 9px;"
        );

        securityEventsBox.getChildren()
                .add(emptyEventsLabel);
    }

    private void removeEmptyEventsMessage() {

        securityEventsBox.getChildren()
                .removeIf(node ->
                        node instanceof Label
                                && ((Label) node)
                                .getText()
                                .equals(
                                        "No security events detected"
                                )
                );
    }

    private void clearSecurityEvents() {

        securityEventsBox.getChildren()
                .clear();

        addEmptyEventsMessage();
    }

    private void stopMonitoring() {

        monitoring = false;

        if (monitoringThread != null) {

            monitoringThread.interrupt();
            monitoringThread = null;
        }

        startMonitoringButton.setDisable(false);
        stopMonitoringButton.setDisable(true);
    }

    private void showError(
            String title,
            String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle("FileGuard");
        alert.setHeaderText(title);
        alert.setContentText(message);

        alert.showAndWait();
    }

    private void showWarning(
            String title,
            String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.WARNING
                );

        alert.setTitle("FileGuard");
        alert.setHeaderText(title);
        alert.setContentText(message);

        alert.showAndWait();
    }

    @Override
    public void stop() {

        stopMonitoring();
    }

    public static void main(String[] args) {
        launch(args);
    }
}