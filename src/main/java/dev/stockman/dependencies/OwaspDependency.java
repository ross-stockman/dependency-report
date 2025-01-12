package dev.stockman.dependencies;

import java.util.List;

public record OwaspDependency(String fileName, List<Vulnerability> vulnerabilities) {
}
