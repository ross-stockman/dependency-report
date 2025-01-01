package dev.stockman.dependencies;

public record DependencyRow(Dependency dependency, boolean[] matrix) implements Comparable<DependencyRow> {
    @Override
    public int compareTo(DependencyRow other) {
        return this.dependency.compareTo(other.dependency);
    }
    public String toCsv() {
        StringBuilder matrixString = new StringBuilder();
        for (int i = 0; i < matrix.length; i++) {
            matrixString.append(matrix[i]);
            if (i < matrix.length - 1) {
                matrixString.append(",");
            }
        }
        return String.format("%s,%s,%s,%s,%s,%s", dependency.groupId(), dependency.artifactId(), dependency.version(), dependency.packageType(), dependency.scope(), matrixString);
    }
}
