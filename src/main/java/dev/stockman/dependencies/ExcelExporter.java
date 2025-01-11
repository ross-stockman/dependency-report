package dev.stockman.dependencies;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ExcelExporter {

    public void exportToExcel(List<DependencyRow> rows, List<String> projects, String filePath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {

            createByScope(workbook, rows, projects);
            createByVersion(workbook, removeScopeAndMerge(rows), projects);
            createByArtifact(workbook, removeScopeAndVersionAndMerge(rows), projects);

            // Write the output to a file
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }

        }
    }

    private void createByScope(Workbook workbook, List<DependencyRow> rows, List<String> projects) {
        Sheet sheet = workbook.createSheet("By Scope");

        // Freeze the top header row
        sheet.createFreezePane(0, 1);
        
        String[] columns = {"Group ID", "Artifact ID", "Version", "Package Type", "Scope"};

        // Create header row
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columns.length; i++) {
            headerRow.createCell(i).setCellValue(columns[i]);
        }
        for (int i = 0; i < projects.size(); i++) {
            headerRow.createCell(columns.length + i).setCellValue(projects.get(i));
        }

        // Create styles
        CellStyle testStyle = workbook.createCellStyle();
        testStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        testStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle compileStyle = workbook.createCellStyle();
        compileStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        compileStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle providedStyle = workbook.createCellStyle();
        providedStyle.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
        providedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle trueStyle = workbook.createCellStyle();
        trueStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        trueStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle falseStyle = workbook.createCellStyle();
        falseStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
        falseStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Create data rows
        for (int i = 0; i < rows.size(); i++) {
            DependencyRow row = rows.get(i);
            Row excelRow = sheet.createRow(i + 1);
            CellStyle rowStyle = null;

            // Determine the style based on scope
            switch (row.dependency().scope()) {
                case "test":
                    rowStyle = testStyle;
                    break;
                case "compile":
                    rowStyle = compileStyle;
                    break;
                case "provided", "runtime":
                    rowStyle = providedStyle;
                    break;
            }

            // Apply style to each cell in the row
            Cell cell = excelRow.createCell(0);
            cell.setCellValue(row.dependency().groupId());
            if (rowStyle != null) cell.setCellStyle(rowStyle);

            cell = excelRow.createCell(1);
            cell.setCellValue(row.dependency().artifactId());
            if (rowStyle != null) cell.setCellStyle(rowStyle);

            cell = excelRow.createCell(2);
            cell.setCellValue(row.dependency().version());
            if (rowStyle != null) cell.setCellStyle(rowStyle);

            cell = excelRow.createCell(3);
            cell.setCellValue(row.dependency().packageType());
            if (rowStyle != null) cell.setCellStyle(rowStyle);

            cell = excelRow.createCell(4);
            cell.setCellValue(row.dependency().scope());
            if (rowStyle != null) cell.setCellStyle(rowStyle);

            for (int j = 0; j < row.matrix().length; j++) {
                cell = excelRow.createCell(columns.length + j);
                cell.setCellValue(row.matrix()[j]);
                if (row.matrix()[j]) {
                    cell.setCellStyle(trueStyle);
                } else {
                    cell.setCellStyle(falseStyle);
                }
            }
        }

        // Auto-size columns
        for (int i = 0; i < columns.length + projects.size(); i++) {
            sheet.autoSizeColumn(i);
        }

        // Add filters to the top row
        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, (columns.length - 1) + projects.size()));
    }

    private void createByVersion(Workbook workbook, List<DependencyRow> rows, List<String> projects) {
        Sheet sheet = workbook.createSheet("By Version");

        // Freeze the top header row
        sheet.createFreezePane(0, 1);

        String[] columns = {"Group ID", "Artifact ID", "Version", "Package Type"};

        // Create header row
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columns.length; i++) {
            headerRow.createCell(i).setCellValue(columns[i]);
        }
        for (int i = 0; i < projects.size(); i++) {
            headerRow.createCell(columns.length + i).setCellValue(projects.get(i));
        }

        // Create styles

        CellStyle trueStyle = workbook.createCellStyle();
        trueStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        trueStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle falseStyle = workbook.createCellStyle();
        falseStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
        falseStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Create data rows
        for (int i = 0; i < rows.size(); i++) {
            DependencyRow row = rows.get(i);
            Row excelRow = sheet.createRow(i + 1);

            // Apply style to each cell in the row
            Cell cell = excelRow.createCell(0);
            cell.setCellValue(row.dependency().groupId());

            cell = excelRow.createCell(1);
            cell.setCellValue(row.dependency().artifactId());

            cell = excelRow.createCell(2);
            cell.setCellValue(row.dependency().version());

            cell = excelRow.createCell(3);
            cell.setCellValue(row.dependency().packageType());

            for (int j = 0; j < row.matrix().length; j++) {
                cell = excelRow.createCell(columns.length + j);
                cell.setCellValue(row.matrix()[j]);
                if (row.matrix()[j]) {
                    cell.setCellStyle(trueStyle);
                } else {
                    cell.setCellStyle(falseStyle);
                }
            }
        }

        // Auto-size columns
        for (int i = 0; i < columns.length + projects.size(); i++) {
            sheet.autoSizeColumn(i);
        }

        // Add filters to the top row
        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, (columns.length - 1) + projects.size()));
    }

    private void createByArtifact(Workbook workbook, List<DependencyRow> rows, List<String> projects) {
        Sheet sheet = workbook.createSheet("By Artifact");

        // Freeze the top header row
        sheet.createFreezePane(0, 1);

        String[] columns = {"Group ID", "Artifact ID", "Package Type"};

        // Create header row
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columns.length; i++) {
            headerRow.createCell(i).setCellValue(columns[i]);
        }
        for (int i = 0; i < projects.size(); i++) {
            headerRow.createCell(columns.length + i).setCellValue(projects.get(i));
        }

        // Create styles
        CellStyle trueStyle = workbook.createCellStyle();
        trueStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        trueStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle falseStyle = workbook.createCellStyle();
        falseStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
        falseStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Create data rows
        for (int i = 0; i < rows.size(); i++) {
            DependencyRow row = rows.get(i);
            Row excelRow = sheet.createRow(i + 1);

            // Apply style to each cell in the row
            Cell cell = excelRow.createCell(0);
            cell.setCellValue(row.dependency().groupId());

            cell = excelRow.createCell(1);
            cell.setCellValue(row.dependency().artifactId());

            cell = excelRow.createCell(2);
            cell.setCellValue(row.dependency().packageType());

            for (int j = 0; j < row.matrix().length; j++) {
                cell = excelRow.createCell(columns.length + j);
                cell.setCellValue(row.matrix()[j]);
                if (row.matrix()[j]) {
                    cell.setCellStyle(trueStyle);
                } else {
                    cell.setCellStyle(falseStyle);
                }
            }
        }

        // Auto-size columns
        for (int i = 0; i < columns.length + projects.size(); i++) {
            sheet.autoSizeColumn(i);
        }

        // Add filters to the top row
        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, (columns.length - 1) + projects.size()));
    }

    private List<DependencyRow> removeScopeAndMerge(List<DependencyRow> dependencies) {
        Map<String, DependencyRow> merged = new java.util.HashMap<>();
        for (DependencyRow dependency : dependencies) {
            String key = dependency.dependency().uniqueVersionId();
            if (merged.containsKey(key)) {
                DependencyRow existing = merged.get(key);
                DependencyRow mergedDependency = DependencyRow.merge(existing.dependency().withoutScope(), existing.matrix(), dependency.matrix());
                merged.put(key, mergedDependency);
            } else {
                merged.put(key, new DependencyRow(dependency.dependency().withoutScope(), dependency.matrix()));
            }
        }
        return merged.values().stream().sorted().toList();
    }
    private List<DependencyRow> removeScopeAndVersionAndMerge(List<DependencyRow> dependencies) {
        Map<String, DependencyRow> merged = new java.util.HashMap<>();
        for (DependencyRow dependency : dependencies) {
            String key = dependency.dependency().uniqueArtifactId();
            if (merged.containsKey(key)) {
                DependencyRow existing = merged.get(key);
                DependencyRow mergedDependency = DependencyRow.merge(existing.dependency().withoutVersionAndScope(), existing.matrix(), dependency.matrix());
                merged.put(key, mergedDependency);
            } else {
                merged.put(key, new DependencyRow(dependency.dependency().withoutVersionAndScope(), dependency.matrix()));
            }
        }
        return merged.values().stream().sorted().toList();
    }
}
