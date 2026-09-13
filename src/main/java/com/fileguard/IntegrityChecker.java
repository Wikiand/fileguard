package com.fileguard;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IntegrityChecker {

    public List<FileChange> compare(
            Map<Path, String> baseline,
            Map<Path, String> current) {

        List<FileChange> changes = new ArrayList<>();

        Set<Path> allPaths = new HashSet<>();
        allPaths.addAll(baseline.keySet());
        allPaths.addAll(current.keySet());

        for (Path path : allPaths) {

            boolean wasInBaseline = baseline.containsKey(path);
            boolean isCurrentlyPresent = current.containsKey(path);

            if (wasInBaseline && !isCurrentlyPresent) {
                changes.add(
                        new FileChange(path, ChangeType.DELETED)
                );

            } else if (!wasInBaseline && isCurrentlyPresent) {
                changes.add(
                        new FileChange(path, ChangeType.NEW)
                );

            } else if (baseline.get(path).equals(current.get(path))) {
                changes.add(
                        new FileChange(path, ChangeType.UNCHANGED)
                );

            } else {
                changes.add(
                        new FileChange(path, ChangeType.MODIFIED)
                );
            }
        }

        return changes;
    }
}
