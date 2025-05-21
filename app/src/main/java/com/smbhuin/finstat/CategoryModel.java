package com.smbhuin.finstat;

import java.util.ArrayList;

public class CategoryModel {

    public String id;
    public String name;
    public ArrayList<String> accounts;
    public ArrayList<String> features;
    public ArrayList<String> contents;

    public CategoryModel() {
        features = new ArrayList<String>();
        contents = new ArrayList<String>();
    }

    public void setNarration(String n) {
        String[] fs = n.split("[^a-zA-Z0-9\\s]");
        for (String f : fs) {
            String fc = clean(f.toLowerCase());
            if (fc.length() != 0 && !this.features.contains(fc)) {
                this.features.add(fc);
            }
        }
    }

    /*public void setNarration(String n) {
        String[] fs = n.replaceAll("[^a-zA-Z0-9]"," ").toLowerCase().trim().split("\\s");
        for (String f : fs) {
            if (f.length() != 0 && !this.hasMaxDigit(f) && !this.features.contains(f)) {
                this.features.add(f);
            }
        }
    }*/

    public void setName(String name) {
        this.name = name;    
    }

    public void addFeature(String f) {
        if (!this.features.contains(f)) {
            this.features.add(f);
        }
    }

    public void addFeatures(ArrayList<String> fs) {
        this.features.addAll(fs);
    }

    public void setId(String id) {
        this.id = id;
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
