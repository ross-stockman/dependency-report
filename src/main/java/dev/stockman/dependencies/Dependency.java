package dev.stockman.dependencies;

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
        result = this.version.compareTo(other.version);
        if (result != 0) return result;
        result = this.scope.compareTo(other.scope);
        if (result != 0) return result;
        return this.packageType.compareTo(other.packageType);
    }
}
