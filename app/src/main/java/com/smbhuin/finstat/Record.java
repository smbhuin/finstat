package com.smbhuin.finstat;

import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;  
import java.util.TimeZone;

public class Record {

    public String date;
    public String bank;
    public String account;
    public String narration;
    public String ref;
    public String type;
    public String amount;
    public String balance;
    public String category;

    public Record() {
    }

    public Boolean hasValidDate() {
        return date.length() == 10;
    }

    public Date getDate() {
        SimpleDateFormat sformatter = new SimpleDateFormat("yyyy-MM-dd"); 
        sformatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {  
            Date dt = sformatter.parse(date);
            return dt;
        } catch (Exception e) {
            e.printStackTrace();
        }  
        return null;
    }

    public double getAmount() {
        return Double.parseDouble(amount);
    }

    public double getBalance() {
        return Double.parseDouble(balance);
    }

    public void merge(Record r) {
        date = date + r.date;
        narration = narration + r.narration;
        ref = ref + r.ref;
        type = type + r.type;
        amount = amount + r.amount;
        balance = balance + r.balance;
    }

    public void mergeNarration(Record r) {
        narration = narration + r.narration;
    }

    public ArrayList<String> features() {
        String[] fs = this.narration.replaceAll("[^a-zA-Z0-9]"," ").toLowerCase().trim().split("\\s");
        ArrayList<String> fl = new ArrayList<String>();
        for (String fsi : fs) {
            if (fsi.length() != 0) {
                fl.add(fsi);
            }
        }
        fl.add(this.type);
        fl.add(this.account);
        return fl;
    }
    
}
