package dev.stockman.dependencies;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public final class OwaspTransformer {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private OwaspTransformer() {}

    private static JsonNode toJson(String json) {
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Vulnerability fromJson(JsonNode json) {
        return OBJECT_MAPPER.convertValue(json, Vulnerability.class);
    }

    private static String removeWhiteSpace(String str) {
        if (str == null) {
            return null; // Return null if the input is null
        }
        return str.replaceAll("[\\t\\n\\r]+", " ") // Replace tabs, newlines, and carriage returns with a single space
                .replaceAll(" +", " ")          // Replace multiple spaces with a single space
                .trim();                        // Remove leading and trailing spaces (optional)
    }

    public static final Function<String, JsonNode> TO_JSON = OwaspTransformer::toJson;

    public static final UnaryOperator<JsonNode> EXTRACT_CVE = json -> {
        ArrayNode output = OBJECT_MAPPER.createArrayNode();
        ArrayNode dependencies = (ArrayNode) json.get("dependencies");
        dependencies.forEach(dependency -> {
            String fileName = dependency.get("fileName").asText();
            if (dependency.path("vulnerabilities").isArray()) {
                ArrayNode vulnerabilities = (ArrayNode) dependency.get("vulnerabilities");
                if (!vulnerabilities.isEmpty()) {
                    vulnerabilities.forEach(vulnerability -> {
                        ObjectNode outputVulnerability = OBJECT_MAPPER.createObjectNode();
                        outputVulnerability.put("fileName", fileName);
                        if (vulnerability.path("name").isValueNode()) {
                            outputVulnerability.put("name", vulnerability.get("name").asText());
                        }
                        if (vulnerability.path("severity").isValueNode()) {
                            outputVulnerability.put("severity", vulnerability.get("severity").asText());
                        }
                        if (vulnerability.path("description").isValueNode()) {
                            outputVulnerability.put("description", removeWhiteSpace(vulnerability.get("description").asText()));
                        }
                        if (vulnerability.path("cvssv3").path("baseScore").isValueNode()) {
                            outputVulnerability.put("cvssv3Score", vulnerability.get("cvssv3").get("baseScore").asText());
                        }
                        output.add(outputVulnerability);
                    });
                }
            }
        });
        return output;
    };

    public static final Function<JsonNode, List<Vulnerability>> TRANSFORM = json -> {
        List<Vulnerability> vulnerabilities = new ArrayList<>();
        json.forEach(vulnerability -> vulnerabilities.add(fromJson(vulnerability)));
        return vulnerabilities;
    };
}
