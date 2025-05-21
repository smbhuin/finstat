package com.smbhuin.finstat;

import java.io.*;
import java.util.*;

import com.google.gson.*;

public class ModelGenerator {

    protected HashMap<String, Object> records;

    public ModelGenerator() {
        records = new HashMap<String, Object>();
    }
    
    public void write(String path) throws Exception {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FileWriter writer = new FileWriter(path);
        gson.toJson(this.records.values(), writer);
        writer.close();
    }

    public void generateCategoryModel(ArrayList<Record> records) throws Exception {
        
        for (Record record : records) {

            String category = record.category;
            String narration = record.narration;

            ArrayList<String> features = features(narration);
            if (features.size() != 0 && category.length() != 0) {
                CategoryModel m = (CategoryModel)this.records.get(category);
                if (m == null) {
                    m = new CategoryModel();
                    m.setId(category);
                    m.setName(category);
                    this.records.put(category, m);
                }
                m.addFeatures(features);
            }
            
        }
    }

    public ArrayList<String> features(String narration) {
        String[] fs = narration.replaceAll("[^a-zA-Z]"," ").toLowerCase().trim().split("\\s");
        ArrayList<String> fl = new ArrayList<String>();
        for (String f : fs) {
            if (f.length() != 0 /*&& !this.hasMaxDigit(f)*/ && !fl.contains(f)) {
                fl.add(f);
            }
        }
        return fl;
    }

    public boolean hasMaxDigit(String s) {
        char[] chars = s.toCharArray();
        int dcount = 0;
        int ccount = 0;
        for (char c : chars){
            if (Character.isDigit(c)){
                dcount++;
            }
            else {
                ccount++;
            }
        }
        if (ccount == 0) {
            return true;
        }
        else {
            return ((dcount-ccount) > 2);
        }
    }

    public String clean(String s) {
        String result = "";
        String[] fs = s.split("\\s");
        for (String f : fs) {
            if (f.length() != 0 && !this.hasMaxDigit(f)) {
                result += f;
                result += " ";
            }
        }
        return result.trim();
    }
    
}
