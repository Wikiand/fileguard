# FileGuard 🛡️

FileGuard is a Java-based file integrity monitoring tool designed to detect unauthorized changes to files using SHA-256 cryptographic hashes.

It provides baseline creation, integrity verification, continuous monitoring, security event logging, and JSON reporting. The project is built as a cybersecurity-focused portfolio project demonstrating practical file integrity monitoring and security auditing concepts.

## Features

* 🔐 SHA-256 file hashing
* 📋 Baseline creation and loading
* 🔎 File integrity verification
* ⚠️ Detection of modified files
* ➕ Detection of newly created files
* ➖ Detection of deleted files
* 👀 Continuous directory monitoring
* 🧾 Baseline vs. current hash reporting
* 🕐 Timestamped monitoring events
* 📜 Security event logging
* ▶️ Monitor startup and shutdown logging
* 📊 JSON verification reports
* 🧪 Automated unit and integration testing
* 📦 Executable Maven JAR

## How It Works

FileGuard uses cryptographic hashes to establish and verify the expected state of files.

### 1. Create a baseline

FileGuard scans the selected directory and calculates a SHA-256 hash for every file.

The resulting hashes are stored in a baseline file.

### 2. Verify integrity

During verification, FileGuard scans the directory again and compares the current hashes against the baseline.

It identifies:

* **UNCHANGED** — file matches the baseline
* **MODIFIED** — file exists but its hash has changed
* **NEW** — file was not present in the baseline
* **DELETED** — file was present in the baseline but is no longer present

### 3. Monitor continuously

The monitor repeatedly scans the directory and reports changes as they occur.

When a change is detected, FileGuard reports both the baseline hash and the current hash.

Example:

```text
⚠ MODIFIED: /home/admin/fileguard/test-files/config.txt
| baseline_hash=ed4488111b6890ff0e80fd66c9b47059cd69bf51c13f1b359c593dbef8a99513
| current_hash=a5bfdc744c4a7d8517291379c36c08e986ae23ca15e46a0166ed4a32a6f3a659
```

## Requirements

* Java 17+
* Maven 3.8+

## Build

Clone the repository and build the project:

```bash
mvn clean package
```

The executable JAR will be created in:

```text
target/fileguard-1.0-SNAPSHOT.jar
```

## Usage

### Create a baseline

```bash
java -jar target/fileguard-1.0-SNAPSHOT.jar scan test-files
```

### Verify a directory

```bash
java -jar target/fileguard-1.0-SNAPSHOT.jar verify test-files
```

Example output:

```text
FileGuard Verification
----------------------
✓ UNCHANGED: /home/admin/fileguard/test-files/users.txt
✓ UNCHANGED: /home/admin/fileguard/test-files/config.txt
✓ UNCHANGED: /home/admin/fileguard/test-files/notes.txt

Verification Summary
--------------------
Unchanged: 3
Modified:  0
New:       0
Deleted:   0

Integrity status: OK
```

### Monitor a directory

```bash
java -jar target/fileguard-1.0-SNAPSHOT.jar monitor test-files
```

The monitor checks the directory every 5 seconds.

Example:

```text
FileGuard Monitor
-----------------
Monitoring: test-files
Interval: 5 seconds

[21:41:18] ⚠ MODIFIED: /home/admin/fileguard/test-files/config.txt
| baseline_hash=...
| current_hash=...

[21:41:18] Integrity status: COMPROMISED
```

Press `Ctrl+C` to stop monitoring.

## Custom Baseline

A custom baseline file can be specified:

```bash
java -jar target/fileguard-1.0-SNAPSHOT.jar scan test-files --baseline baseline.txt
```

Then verify against it:

```bash
java -jar target/fileguard-1.0-SNAPSHOT.jar verify test-files --baseline baseline.txt
```

## JSON Reports

Verification results can be exported as JSON:

```bash
java -jar target/fileguard-1.0-SNAPSHOT.jar verify test-files --report report.json
```

Example structure:

```json
{
  "unchanged": 2,
  "modified": 1,
  "newFiles": 0,
  "deleted": 0,
  "compromised": true,
  "changes": [
    {
      "path": "/home/admin/fileguard/test-files/config.txt",
      "type": "MODIFIED",
      "baselineHash": "old-hash",
      "currentHash": "new-hash"
    }
  ]
}


## Monitoring Logs

FileGuard records monitoring events in:

```text
fileguard-monitor.log
```

Example:

```text
[2026-09-13 21:41:03] MONITOR_STARTED: /home/admin/fileguard/test-files | interval=5s
[2026-09-13 21:41:18] ⚠ MODIFIED: /home/admin/fileguard/test-files/config.txt | baseline_hash=... | current_hash=...
[2026-09-13 21:41:18] Integrity status: COMPROMISED
[2026-09-13 21:42:00] MONITOR_STOPPED
```

## Project Structure

```text
fileguard/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── fileguard/
│   │               ├── BaselineManager.java
│   │               ├── ChangeType.java
│   │               ├── FileChange.java
│   │               ├── FileGuardException.java
│   │               ├── FileHasher.java
│   │               ├── FileMonitor.java
│   │               ├── FileScanner.java
│   │               ├── IntegrityChecker.java
│   │               ├── JsonReportWriter.java
│   │               ├── Main.java
│   │               ├── MonitorLogger.java
│   │               └── VerificationReport.java
│   │
│   └── test/
│       └── java/
│           └── com/
│               └── fileguard/
│                   └── ...tests...
│
├── pom.xml
├── .gitignore
└── README.md
```

## Testing

FileGuard includes automated tests covering hashing, scanning, baselines, integrity checking, monitoring, logging, and JSON reporting.

Run the test suite with:

```bash
mvn test
```

Current test suite:

```text
Tests run: 17
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

## Security Concepts Demonstrated

FileGuard was built to explore practical cybersecurity concepts including:

* File integrity monitoring
* Cryptographic hashing
* SHA-256
* Baseline comparison
* Tamper detection
* Security event logging
* Audit trails
* Integrity status monitoring
* Automated security testing

🚧 Still in Development

File Guard is currently under active development.

The core file monitoring and integrity functionality is being developed and tested incrementally. Some features described in the roadmap are planned future improvements and are not yet implemented.

Current development is focused on:

* Core file and directory monitoring
* File creation, modification, and deletion detection
* SHA-256 integrity verification
* Security event logging
* Automated testing
* Improving reliability and error handling

🔮 Future Development

Planned improvements include:

* Real-time security alerts
* Persistent event storage
* REST API
* Web dashboard
* User authentication and authorization
* Advanced threat detection
* Docker deployment
* Cloud integration
* AI-assisted security event analysis

This project is evolving. Features, architecture, and implementation details may change as development continues.

## Future Development

The current CLI version provides the core integrity-monitoring engine.

The next development phase will transform FileGuard into a **desktop cybersecurity application** with a graphical interface while keeping the existing integrity-monitoring engine underneath.

Planned application capabilities include:

* Graphical dashboard
* Folder selection
* Start/stop monitoring controls
* Live integrity status
* Security event display
* File change visualization
* Monitoring history


## Author

**Christine  Nyambura **

Software Developer with a focus on Cybersecurity and AI.
