package dev.stockman.dependencies;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelExporter {

    public void exportToExcel(List<DependencyRow> rows, List<String> projects, String filePath) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Dependencies");

        // Freeze the top header row
        sheet.createFreezePane(0, 1);

        // Create header row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Group ID");
        headerRow.createCell(1).setCellValue("Artifact ID");
        headerRow.createCell(2).setCellValue("Version");
        headerRow.createCell(3).setCellValue("Package Type");
        headerRow.createCell(4).setCellValue("Scope");
        for (int i = 0; i < projects.size(); i++) {
            headerRow.createCell(5 + i).setCellValue(projects.get(i));
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
                cell = excelRow.createCell(5 + j);
                cell.setCellValue(row.matrix()[j]);
                if (row.matrix()[j]) {
                    cell.setCellStyle(trueStyle);
                } else {
                    cell.setCellStyle(falseStyle);
                }
            }
        }

        // Auto-size columns
        for (int i = 0; i < 5 + projects.size(); i++) {
            sheet.autoSizeColumn(i);
        }

        // Add filters to the top row
        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, 4 + projects.size()));

        // Write the output to a file
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        }

        // Closing the workbook
        workbook.close();
    }
}
