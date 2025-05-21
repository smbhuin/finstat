package com.smbhuin.finstat;

import java.io.*;
import java.util.*;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.text.SimpleDateFormat;  

import de.daslaboratorium.machinelearning.classifier.*;
import de.daslaboratorium.machinelearning.classifier.bayes.*;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.Table;
import technology.tabula.PageIterator;
import technology.tabula.RectangularTextContainer;
import technology.tabula.extractors.BasicExtractionAlgorithm;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

public class Statement {

    protected ArrayList<Record> records;
    private File file;
    private String password;
    private StatementModel statementType;

    public Statement() {
        this.records = new ArrayList<Record>();
        this.file = null;
    }

    public Statement(File file, String password, StatementModel type) {
        this.records = new ArrayList<Record>();
        this.file = file;
        this.password = password;
        this.statementType = type;
    }

    public String getPassword() {
        return password;
    }

    public ArrayList<Record> getRecords() {
        return this.records;
    }

    public String getFormattedDate(String date, String format) {
        SimpleDateFormat sformatter = new SimpleDateFormat(format);
        SimpleDateFormat dformatter = new SimpleDateFormat("yyyy-MM-dd");
        try {  
            Date dt = sformatter.parse(date);
            return dformatter.format(dt);
        } catch (Exception e) {e.printStackTrace();}  
        return null;
    }

    public String getFormattedDate(String date, List<String> formats) {
        SimpleDateFormat dformatter = new SimpleDateFormat("yyyy-MM-dd");
        for (String format : formats) {
            Locale locale = Locale.forLanguageTag("en");
            if (this.statementType != null) {
                locale = Locale.forLanguageTag(this.statementType.locale);
            }
            SimpleDateFormat sformatter = new SimpleDateFormat(format, locale);
            try {
                Date dt = sformatter.parse(date);
                return dformatter.format(dt);
            } catch (Exception e) {
                // e.printStackTrace();
                // Continue trying other formats
            }
        }
        return null;
    }

    public void add(Record r) {
        records.add(r);
    }

    public void appendStatement(Statement s) {
        records.addAll(s.records);
    }

    public void merge(Record r) {
        if (records.size() != 0) {
            Record x = records.get(records.size()-1);
            x.merge(r);
        }
    }

    public void mergeNarration(Record r) {
        if (records.size() != 0) {
            Record x = records.get(records.size()-1);
            x.mergeNarration(r);
        }
    }

    public Record lastRecord() {
        if (records.size() != 0) {
            return records.get(records.size()-1);
        }
        return null;
    }

    public void parse() throws Exception {
        int dateIndex = this.statementType.columnIndices.get("date").intValue();
        int narrationIndex = this.statementType.columnIndices.get("narration").intValue();
        int refIndex = this.statementType.columnIndices.getOrDefault("ref", Integer.valueOf(-1)).intValue();
        int debitIndex = this.statementType.columnIndices.getOrDefault("debit", Integer.valueOf(-1)).intValue();
        int creditIndex = this.statementType.columnIndices.getOrDefault("credit", Integer.valueOf(-1)).intValue();
        int amountIndex = this.statementType.columnIndices.getOrDefault("amount", Integer.valueOf(-1)).intValue();
        int typeIndex = this.statementType.columnIndices.getOrDefault("type", Integer.valueOf(-1)).intValue();
        int balanceIndex = this.statementType.columnIndices.getOrDefault("balance", Integer.valueOf(-1)).intValue();
        int minDateLength = this.statementType.minDateLength();
        InputStream in = new FileInputStream(this.file);
        PDDocument document = PDDocument.load(in, getPassword());
        BasicExtractionAlgorithm bea = new BasicExtractionAlgorithm();
        SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
        ObjectExtractor extractor = new ObjectExtractor(document);
        PageIterator pi = extractor.extract();
        boolean foundStartIndex = false;
        boolean foundEndIndex = false;
        while (pi.hasNext()) {
            // iterate over the pages of the document
            Page page = pi.next();
            if (this.statementType.area.size() == 4) {
                page = page.getArea(
                    this.statementType.area.get(0),
                    this.statementType.area.get(1),
                    this.statementType.area.get(2),
                    this.statementType.area.get(3)
                );
            }
            List<Table> tables = null;
            if (this.statementType.strategy.equals("stream")) {
                tables = bea.extract(page, this.statementType.columns);
            }
            else {
                tables = sea.extract(page);
            }
            // iterate over the tables of the page
            for(Table table: tables) {

                if (table.getColCount() < 3) continue;

                @SuppressWarnings("rawtypes")
                List<List<RectangularTextContainer>> rows = table.getRows();
                
                for (@SuppressWarnings("rawtypes") List<RectangularTextContainer> cells : rows) {

                    StringBuilder lineBuilder = new StringBuilder();
                    for (@SuppressWarnings("rawtypes") RectangularTextContainer content : cells) {
                        // Note: Cell.getText() uses \r to concat text chunks
                        String text = content.getText().replace("\r", " ");
                        lineBuilder.append(text);
                    }
                    String line = lineBuilder.toString().trim();

                    // Find the first valid record
                    if (foundStartIndex == false && line.contains(this.statementType.startLine)) {
                        foundStartIndex = true;
                        continue;
                    }
                    if (foundStartIndex == false) {
                        continue;
                    }
                    // Find the last record (invalid record)
                    if (foundEndIndex == false && line.contains(this.statementType.endLine)) {
                        foundEndIndex = true;
                        break;
                    }
                    if (foundEndIndex == true) {
                        break;
                    }

                    boolean foundBadLine = false;
                    for (String str : this.statementType.ignoreLines) {
                        if (line.contains(str)) {
                            foundBadLine = true;
                            break;
                        }
                    }
                    if (foundBadLine || line.isEmpty()) {
                        continue;
                    }

                    Record r = new Record();
                    r.bank = this.statementType.bank;
                    r.account = this.statementType.accountType;
                    r.date = "";

                    String date = cells.get(dateIndex).getText().replace("\r", " ").trim();
                    
                    if(date != null) {
                        r.date = date;
                    }
                    
                    r.narration = cells.get(narrationIndex).getText().replace("\r", " ");
                    // If narration is blank that means there is no valid records
                    
                    if (refIndex != -1) {
                        r.ref = cells.get(refIndex).getText().replace("\r", " ");
                    }
                    else {
                        r.ref = "";
                    }
                    if (amountIndex != -1) {
                        String amount = cells.get(amountIndex).getText().replace("\r", " ").trim().toLowerCase();

                        if (typeIndex != -1) {
                            String amtType = cells.get(typeIndex).getText().replace("\r", " ").trim().toLowerCase();
                            if(amount.length() != 0) {
                                r.amount = amount.replace(",", "");
                            } else if(amount.length() != 0) {
                                r.amount = amount.replace(",", "");
                            }
                            else {
                                r.amount = "";
                            }
                            if(amtType.startsWith("cr")) {
                                r.type = RecordType.CREDIT;
                            } else if(amtType.startsWith("dr")) {
                                r.type = RecordType.DEBIT;
                            }
                            else {
                                r.type = RecordType.DEBIT;;
                            }
                        }
                        else {
                            if(amount.contains("cr")) {
                                r.amount = amount.replace("cr","").replace(",","").trim();
                                r.type = RecordType.CREDIT;
                            } else if(amount.length() != 0){
                                r.amount = amount.replace(",","").trim();
                                r.type = RecordType.DEBIT;
                            }
                            else {
                                r.amount = "";
                                r.type = "";
                            }
                        }
                        
                    }
                    else {
                        String debit = cells.get(debitIndex).getText().replace("\r", " ").trim();
                        String credit = cells.get(creditIndex).getText().replace("\r", " ").trim();
                        if(debit.length() != 0) {
                            r.amount = debit.replace(",", "");
                            r.type = RecordType.DEBIT;
                        } else if(credit.length() != 0) {
                            r.amount = credit.replace(",", "");
                            r.type = RecordType.CREDIT;
                        }
                        else {
                            r.amount = "";
                            r.type = "";
                        }
                    }
                    if (balanceIndex != -1) {
                        r.balance = cells.get(balanceIndex).getText().replace("\r", " ").replace(",","");
                    }
                    else {
                        r.balance = "0.00";
                    }
                    Record lr = this.lastRecord();

                    // merge if previous record date is incomplete and current record date is incomplete
                    if (this.statementType.recordTopAligned) {
                        if(lr != null && ((lr.date.length() == minDateLength && r.date.isEmpty())
                        || (lr.date.length() < minDateLength && r.date.length() < minDateLength))
                        ) {
                            lr.merge(r);
                        }
                        else {
                            this.add(r);
                        }
                    }
                    else {
                        if(lr != null && lr.date.length() < minDateLength) {
                            lr.merge(r);
                        }
                        else {
                            this.add(r);
                        }
                    }
                }
            }
        }
        extractor.close();
    
        for (Record record : records) {
            record.date = this.getFormattedDate(record.date, this.statementType.dateFormats);
        }
    }

    public void write(String path) throws Exception {
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(path));
        
        CSVPrinter printer = new CSVPrinter(writer, 
            CSVFormat.Builder
            .create()
            .setDelimiter(";")
            .setRecordSeparator("\n")
            .setEscape('\\')
            .setQuoteMode(QuoteMode.MINIMAL)
            .setHeader("date", "bank", "account", "narration", "ref", "type", "amount", "balance", "category")
            .build()
        );
        
        for (Record r : records) {
            printer.printRecord(r.date, r.bank, r.account, r.narration, r.ref, r.type, r.amount, r.balance, r.category);
        }
        printer.flush();
        printer.close();
    }

    public void classifySmart(List<CategoryModel> categories) throws Exception {
        // Create a new bayes classifier with string categories and string features.
        Classifier<String, String> bayes = new BayesClassifier<String, String>();

        for (CategoryModel c : categories) {
            bayes.learn(c.id, c.features);
            bayes.learn(c.id, c.accounts);
        }

        for (Record r : records) {
            Classification<String, String> cls = bayes.classify(r.features());
            //if (cls.getProbability() > 0.0000011) {
            r.category = cls.getCategory();
            //}

            for (CategoryModel c : categories) {
                for (String f : c.contents) {
                    if (r.narration.toLowerCase().contains(f)) {
                        r.category = c.id;
                    }
                }
            }

        }
    }

    public void classifyMatch(List<CategoryModel> categories) throws Exception {
        for (Record r : records) {
            for (CategoryModel c : categories) {
                for (String f : c.features) {
                    if (r.narration.toLowerCase().contains(f)) {
                        r.category = c.id;
                    }
                }
            }
        }
    }

    public void classifyBayes(List<CategoryModel> categories) throws Exception {

        // Create a new bayes classifier with string categories and string features.
        Classifier<String, String> bayes = new BayesClassifier<String, String>();

        for (CategoryModel c : categories) {
            bayes.learn(c.id, c.features);
            bayes.learn(c.id, c.accounts);
        }

        for (Record r : records) {
            Classification<String, String> cls = bayes.classify(r.features());
            //if (cls.getProbability() > 0.0000011) {
            r.category = cls.getCategory();
            //}
        }

    }

}
