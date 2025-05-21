package com.smbhuin.finstat;

import java.io.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Date;

import org.apache.poi.xssf.usermodel.*;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.openxml4j.util.ZipSecureFile;

public class Finstat {

    private Statement stmt;
    private List<StatementModel> statementTypes;
    private List<CategoryModel> categories;

    public Finstat() {
        stmt = new Statement();
        statementTypes = new ArrayList<StatementModel>();
        categories = new ArrayList<CategoryModel>();
    }

    public void setCategoryModelFile(String path) throws Exception {
        this.loadCategories(path);
    }

    public void setCategoryModelFile(File file) throws Exception {
        this.loadCategories(file);
    }

    public void setStatementModelFile(String path) throws Exception {
        this.loadStatements(path);
    }

    public void setStatementModelFile(File file) throws Exception {
        this.loadStatements(file);
    }

    public void loadCategories(String file) throws Exception { // loads file as internal resource
        Gson gson = new Gson();
        InputStream in = getClass().getResourceAsStream(file);
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        CategoryModel[] objects = gson.fromJson(reader, CategoryModel[].class);
        for (CategoryModel c : objects) {
            categories.add(c);
        }
    }

    public void loadCategories(File file) throws Exception {
        Gson gson = new Gson();
        InputStream in = new FileInputStream(file);
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        CategoryModel[] objects = gson.fromJson(reader, CategoryModel[].class);
        for (CategoryModel c : objects) {
            categories.add(c);
        }
    }

    public void loadStatements(String file)  throws Exception {
        Gson gson = new Gson();
        InputStream in = getClass().getResourceAsStream(file);
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        StatementModel[] objects = gson.fromJson(reader, StatementModel[].class);
        for (StatementModel c : objects) {
            statementTypes.add(c);
        }
    }

    public void loadStatements(File file)  throws Exception {
        Gson gson = new Gson();
        InputStream in = new FileInputStream(file);
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        StatementModel[] objects = gson.fromJson(reader, StatementModel[].class);
        for (StatementModel c : objects) {
            statementTypes.add(c);
        }
    }

    public void generateModel(String path)  throws Exception {
        ModelGenerator mg = new ModelGenerator();
        mg.generateCategoryModel(stmt.getRecords());
        mg.write(path);
    }

    public void generateCSV(String path)  throws Exception {
        stmt.write(path);
    }

    public void generateXLSX(String path)  throws Exception {
        ReportGenerator rg = new ReportGenerator(stmt.getRecords(), categories, statementTypes);
        rg.generateXLSX(path);
    }
    
    public void process(File file, String password, String classifier) throws Exception {
        boolean processed = false;
        for (StatementModel type : statementTypes) {
            if (file.getName().toLowerCase().startsWith(type.id)) {
                Statement s = new Statement(file, password, type);
                s.parse();
                if (classifier.equals("bayes")) {
                    s.classifyBayes(categories);
                }
                if (classifier.equals("match")) {
                    s.classifyMatch(categories);
                }
                else {
                    s.classifySmart(categories);
                }
                stmt.appendStatement(s);
                processed = true;
                break;
            }
        }
        if (processed == false) {
            System.err.println("File not recognized: " + file.toString());
        }
    }

    public void loadReport(File file) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        ZipSecureFile.setMinInflateRatio(0);
        XSSFWorkbook workbook = new XSSFWorkbook(fis);
        XSSFSheet spreadsheet = workbook.getSheetAt(0);
        Iterator <Row> rowIterator = spreadsheet.iterator();
        boolean skipFirst = true;
        while (rowIterator.hasNext()) {
            XSSFRow row = (XSSFRow) rowIterator.next();
            if (skipFirst) {
                skipFirst = false;
                continue;
            }
            Record r = new Record();
            Date date = row.getCell(0).getDateCellValue();
            r.date = Util.stringFromDate(date);
            r.bank = row.getCell(1).getStringCellValue();
            r.account = row.getCell(2).getStringCellValue();
            r.narration = row.getCell(3).getStringCellValue();
            r.ref = row.getCell(4).getStringCellValue();
            r.type = row.getCell(5).getStringCellValue();
            r.amount = Double.toString(row.getCell(6).getNumericCellValue());
            r.balance = Double.toString(row.getCell(7).getNumericCellValue());
            r.category = row.getCell(8).getStringCellValue();
            stmt.add(r);
        }
        fis.close();
        workbook.close();
    }

    public ArrayList<Record> getRecords() {
        return stmt.getRecords();
    }
    
}
