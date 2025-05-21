package com.smbhuin.finstat;

import java.io.*;
import java.util.concurrent.Callable;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "finstat", mixinStandardHelpOptions = true, version = "1.0.0",
         description = "Generates xlsx report from pdf bank statements.")
public class Main implements Callable<Integer> {

    static String lastPassword = null;

    @Option(names = { "-t", "--task" }, description = "The task to perform. Options: generate-report or generate-model")
    private String task = "generate-report";

    @Option(names = { "-c", "--classifier" }, description = "The classification algorithm to use. Options: smart, bayes, match")
    private String classifier = "smart";

    @Option(names = { "-m", "--category-model" }, description = "Custom categories model file path. ex: './categories.json'")
    File model;

    @Option(names = { "-s", "--statement-model" }, description = "Custom bank statments model file path. ex: './statements.json'")
    File statement;

    @Option(names = { "-r", "--report" }, description = "Report file path. ex; './report.xlsx'")
    File report;

    @Option(names = { "-h", "--help" }, usageHelp = true, description = "Display a help message.")
    private boolean helpRequested = false;

    @Parameters(paramLabel = "FILE", description = "One or more file or directory to process. ex: './statements' 'hdfc_1.pdf' 'axis_1.pdf'")
    File[] files;

    private File[] filesInDir(File dir) {
        File[] matchingFiles = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith("pdf") || name.endsWith("PDF");
            }
        });
        return matchingFiles;
    }

    @Override
    public Integer call() throws Exception {

        if (task.equals("generate-report")) {
            Finstat processor = new Finstat();

            if (statement != null && statement.canRead()) {
                processor.setStatementModelFile(statement);
            }
            else {
                processor.setStatementModelFile("/statements.json");
            }
            
            if (model != null && model.canRead()) {
                processor.setCategoryModelFile(model);
            }
            else {
                processor.setCategoryModelFile("/categories.json");
            }

            for(File c : files) {
                if (c.isDirectory()) {
                    File[] dirFiles = filesInDir(c);
                    for(File df : dirFiles) {
                        String password = askForPasswordIfProtected(df);
                        processor.process(df, password, classifier);
                    }
                }
                else {
                    String password = askForPasswordIfProtected(c);
                    processor.process(c, password, classifier);
                }
            }

            if (report != null) {
                processor.generateXLSX(report.getPath());
            }
            else {
                processor.generateXLSX("report.xlsx");
            }
        }
        else if (task.equals("generate-model")){
            if (report != null && report.isFile() && report.exists()) {
                Finstat processor = new Finstat();
                processor.loadReport(report);

                if (model != null) {
                    processor.generateModel(model.getPath());
                }
                else {
                    processor.generateModel("model.json");
                }
                
            }
            else {
                System.out.println("Report does'nt exists. Please generate report first.");
            }
        }
        else {
            System.out.println("Invalid finstat command");
        }
        
        return 0;
    }

    public static void main(String[] args) {
        // System.out.println("Working Directory = " + System.getProperty("user.dir"));
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    static String askForPasswordIfProtected(File f) {
        String password = null;
        boolean foundPassword = false;
        while (foundPassword == false) {
            try {
                PDDocument document = PDDocument.load(f, password);
                document.close();
                foundPassword = true;
                if (password != null) {
                    lastPassword = password;
                }
            }
            catch (InvalidPasswordException e) {
                if (lastPassword != null) {
                    password = lastPassword;
                    lastPassword = null;
                }
                else {
                    Console console = System.console();
                    if (console == null) {
                        System.err.println("Couldn't get Console instance.");
                        System.exit(1);
                    }
                    else {
                        char[] passwordArray = console.readPassword("Enter password for " + f.toString() + ":");
                        password = new String(passwordArray);
                    }
                }
            }
            catch (Exception e) {
                System.exit(1);
            }
        }
        return password;
    }
    
}