package dev.stockman.dependencies;

public record DependencyRaw(Dependency dependency, String project) implements Comparable<DependencyRaw> {
    @Override
    public int compareTo(DependencyRaw other) {
        return this.dependency.compareTo(other.dependency);
    }
}
