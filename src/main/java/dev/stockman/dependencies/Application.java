package dev.stockman.dependencies;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootApplication
public class Application implements CommandLineRunner {

    @Value("${projects.ignore}")
    private String projectsToIgnore;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length > 0) {
            String sourceFile = args[0];
            String outputFile = args[1];
            Pattern pattern = Pattern.compile(projectsToIgnore);
            List<DependencyRaw> dependencies = new ArrayList<>(Files.readAllLines(Paths.get(sourceFile)).stream().distinct().filter(line -> !line.isBlank()).map(line -> {
                String[] parts = line.split(",");
                return new DependencyRaw(new Dependency(parts[1], parts[2], parts[3], parts[4], parts[5]), parts[0]);
            }).filter(dependencyRaw -> {
                Matcher matcher = pattern.matcher(dependencyRaw.project());
                return !matcher.matches();
            }).sorted().toList());
            List<String> projects = new ArrayList<>(dependencies.stream().map(DependencyRaw::project).distinct().toList());
            Map<String, Integer> projectsPosition  = new HashMap<>();
            for (int i = 0; i < projects.size(); i++) {
                projectsPosition.put(projects.get(i), i);
            }
            Map<Dependency, boolean[]> matrix = new HashMap<>();
            dependencies.forEach(raw -> {
               if (matrix.containsKey(raw.dependency())) {
                   var item = matrix.get(raw.dependency());
                   item[projectsPosition.get(raw.project())] = true;
               } else {
                   boolean[] item = new boolean[projects.size()];
                   item[projectsPosition.get(raw.project())] = true;
                   matrix.put(raw.dependency(), item);
               }
            });
            List<DependencyRow> rows = new ArrayList<>(matrix.entrySet().stream().map(entry -> new DependencyRow(entry.getKey(), entry.getValue())).sorted().toList());
            ExcelExporter excelExporter = new ExcelExporter();
            excelExporter.exportToExcel(rows, projects, outputFile);
        } else {
            System.out.println("Please provide the path to the source file as an argument.");
            System.out.println("mvn spring-boot:run -Ddependencies=\"/path/to/dependency-report.csv\" -Dreport=\"/path/to/dependency-report.xlsx\"");
        }
    }
}
