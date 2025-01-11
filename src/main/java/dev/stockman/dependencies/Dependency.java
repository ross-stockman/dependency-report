package dev.stockman.dependencies;

import java.util.Comparator;
import java.util.Objects;

public record Dependency(String groupId, String artifactId, String version, String packageType, String scope) implements Comparable<Dependency> {
    @Override
    public int compareTo(Dependency other) {
        // First, handle the special rule for the "test" scope
        if ("test".equals(this.scope) && !"test".equals(other.scope)) {
            return 1; // "this" is test, so it goes to the end
        } else if (!"test".equals(this.scope) && "test".equals(other.scope)) {
            return -1; // "other" is test, so it goes to the end
        }
        // Existing comparison logic if neither or both are "test"
        int result = this.groupId.compareTo(other.groupId);
        if (result != 0) return result;
        result = this.artifactId.compareTo(other.artifactId);
        if (result != 0) return result;

        // Null-safe comparison for version
        result = Objects.compare(this.version, other.version, Comparator.nullsFirst(String::compareTo));
        if (result != 0) return result;

        // Null-safe comparison for scope
        result = Objects.compare(this.scope, other.scope, Comparator.nullsFirst(String::compareTo));
        if (result != 0) return result;

        // Comparison for packageType (this is assumed to be non-null)
        return this.packageType.compareTo(other.packageType);
    }

    public String uniqueFullId() {
        return String.format("%s:%s:%s:%s", groupId, artifactId, version, scope);
    }
    public String uniqueVersionId() {
        return String.format("%s:%s:%s", groupId, artifactId, version);
    }
    public String uniqueArtifactId() {
        return String.format("%s:%s", groupId, artifactId);
    }
    public Dependency withoutScope() {
        return new Dependency(groupId, artifactId, version, packageType, null);
    }
    public Dependency withoutVersionAndScope() {
        return new Dependency(groupId, artifactId, null, packageType, null);
    }
}
