package com.smbhuin.finstat;

import java.io.*;

import java.text.DecimalFormat;
import java.util.*;

import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.*;

public class ReportGenerator {

    private List<Record> records;
    private List<CategoryModel> categories;
    private List<StatementModel> statementTypes;
    private DecimalFormat decimalFormatter;
    
    public ReportGenerator(List<Record> records, List<CategoryModel> categories, List<StatementModel> statementTypes) {
        this.records = records;
        this.statementTypes = statementTypes;
        this.categories = categories;
        decimalFormatter = new DecimalFormat();
        decimalFormatter.setMaximumFractionDigits(2);
        decimalFormatter.setMinimumFractionDigits(2);
        decimalFormatter.setGroupingUsed(true);
        decimalFormatter.setParseBigDecimal(true);

    }

    public void generateXLSX(String reportPath) throws Exception {
        // find unique account types
        Set<String> accountTypes = new HashSet<>();
        for (StatementModel stmt : statementTypes) {
            accountTypes.add(stmt.accountType);
        }

        String transactionSheetName = "Transactions";
        String summarySheetName = "Summary";

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet spreadsheet = workbook.createSheet(transactionSheetName);
        spreadsheet.setColumnWidth(0, 2500);
        spreadsheet.setColumnWidth(1, 5000);
        spreadsheet.setColumnWidth(3, 15000);
        spreadsheet.setColumnWidth(4, 8000);
        spreadsheet.setColumnWidth(8, 4000);
        spreadsheet.setAutoFilter(new CellRangeAddress(0, 0, 0, 10));
        spreadsheet.createFreezePane(0, 1);
        spreadsheet.setDefaultRowHeight((short)60); // ??

        // Header Row Styles
        XSSFCellStyle hstyle = workbook.createCellStyle();
        hstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        hstyle.setBorderBottom(BorderStyle.THIN);
        hstyle.setBorderLeft(BorderStyle.THIN);
        hstyle.setBorderTop(BorderStyle.THIN);
        hstyle.setBorderRight(BorderStyle.THIN);
        hstyle.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        hstyle.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        hstyle.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        hstyle.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        hstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        hstyle.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());

        XSSFFont hfont = workbook.createFont();
        hfont.setFontHeightInPoints((short)10);
        hfont.setFontName("Helvetica Neue");
        hstyle.setFont(hfont);

        // Data Row Styles
        XSSFCellStyle dstyle = workbook.createCellStyle();
        XSSFFont dfont = workbook.createFont();
        dfont.setFontHeightInPoints((short)10);
        dfont.setFontName("Helvetica Neue");
        dstyle.setFont(dfont);

        XSSFRow headerRow = spreadsheet.createRow(0);
        XSSFCell headerCell = headerRow.createCell(0);
        headerCell.setCellStyle(hstyle);
        headerCell.setCellValue("Date");
        headerCell = headerRow.createCell(1);
        headerCell.setCellStyle(hstyle);
        headerCell.setCellValue("Bank");
        headerCell = headerRow.createCell(2);
        headerCell.setCellStyle(hstyle);
        headerCell.setCellValue("Account");
        headerCell = headerRow.createCell(3);
        headerCell.setCellStyle(hstyle);
        headerCell.setCellValue("Narration");
        headerCell = headerRow.createCell(4);
        headerCell.setCellStyle(hstyle);
        headerCell.setCellValue("Ref");
        headerCell = headerRow.createCell(5);
        headerCell.setCellStyle(hstyle);
        headerCell.setCellValue("Type");
        headerCell = headerRow.createCell(6);
        headerCell.setCellStyle(hstyle);
        headerCell.setCellValue("Amount");
        headerCell = headerRow.createCell(7);
        headerCell.setCellStyle(hstyle);
        headerCell.setCellValue("Balance");
        headerCell = headerRow.createCell(8);
        headerCell.setCellStyle(hstyle);
        headerCell.setCellValue("Category");

        int rowIndex = 1;
        for (Record r : records) {
            XSSFRow row = spreadsheet.createRow(rowIndex++);
            XSSFCell dateCell = row.createCell(0);

            XSSFCellStyle style = workbook.createCellStyle();  
            style.setDataFormat(workbook.createDataFormat().getFormat("yyyy-mm-dd"));  
            style.setFont(dfont);
            dateCell.setCellStyle(style);
            dateCell.setCellValue(r.getDate());

            XSSFCell cell = row.createCell(1);
            cell.setCellStyle(dstyle);
            cell.setCellValue(r.bank);
            cell = row.createCell(2);
            cell.setCellStyle(dstyle);
            cell.setCellValue(r.account);
            cell = row.createCell(3);
            cell.setCellStyle(dstyle);
            cell.setCellValue(r.narration);
            cell = row.createCell(4);
            cell.setCellStyle(dstyle);
            cell.setCellValue(r.ref);
            cell = row.createCell(5);
            cell.setCellStyle(dstyle);
            cell.setCellValue(r.type);
            cell = row.createCell(6);
            cell.setCellStyle(dstyle);
            cell.setCellValue(r.getAmount());
            cell = row.createCell(7);
            cell.setCellStyle(dstyle);
            cell.setCellValue(r.getBalance());
            cell = row.createCell(8);
            cell.setCellStyle(dstyle);
            cell.setCellValue(r.category);
        }

        // Summary Sheet
        XSSFSheet summarySheet = workbook.createSheet(summarySheetName);
        summarySheet.setColumnWidth(0, 4000);
        summarySheet.setColumnWidth(1, 8000);
        summarySheet.setColumnWidth(2, 4000);
        int summaryRowIndex = 0;

        XSSFRow row = summarySheet.createRow(summaryRowIndex++);
        XSSFCell cell = row.createCell(0);
        cell.setCellStyle(dstyle);
        cell = row.createCell(1);
        cell.setCellStyle(dstyle);
        cell.setCellValue("Transaction Start Date");
        cell = row.createCell(2);
        cell.setCellStyle(dstyle);
        cell.setCellFormula(String.format("MIN(%s!A2:A%d)",transactionSheetName,rowIndex));

        row = summarySheet.createRow(summaryRowIndex++);
        cell = row.createCell(0);
        cell.setCellStyle(dstyle);
        cell = row.createCell(1);
        cell.setCellStyle(dstyle);
        cell.setCellValue("Transaction End Date");
        cell = row.createCell(2);
        cell.setCellStyle(dstyle);
        cell.setCellFormula(String.format("MAX(%s!A2:A%d)",transactionSheetName,rowIndex));

        for (String account : accountTypes) {
            row = summarySheet.createRow(summaryRowIndex++);
            cell = row.createCell(0);
            cell.setCellStyle(hstyle);
            cell = row.createCell(1);
            cell.setCellStyle(hstyle);
            cell.setCellValue(account);
            cell = row.createCell(2);
            cell.setCellStyle(hstyle);

            for (CategoryModel category : categories) {
                if (category.accounts.contains(account)) {
                    row = summarySheet.createRow(summaryRowIndex++);
                    cell = row.createCell(0);
                    cell.setCellStyle(dstyle);
                    cell.setCellValue(category.id);
                    cell = row.createCell(1);
                    cell.setCellStyle(dstyle);
                    cell.setCellValue(category.name);
                    cell = row.createCell(2);
                    cell.setCellStyle(dstyle);
                    cell.setCellFormula(String.format("SUMIFS(%s!$G$2:$G$%d, %s!$C$2:$C$%d,\"%s\", %s!$I$2:$I$%d,%s!A%d)",transactionSheetName,rowIndex,transactionSheetName,rowIndex,account,transactionSheetName,rowIndex,summarySheetName,summaryRowIndex));
                }
            }
        }
        
        // Create file system using specific name
        FileOutputStream out = new FileOutputStream(new File(reportPath));
        workbook.write(out);
        out.close();
        workbook.close();
    }

}
