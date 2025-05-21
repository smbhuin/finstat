package com.smbhuin.finstat;

import java.util.Date;
import java.text.SimpleDateFormat;  

public final class Util {

    public static String currentTimestamp() {
        try {  
            SimpleDateFormat dformatter = new SimpleDateFormat("yyyyMMddHHmmss");  
            return dformatter.format(new Date());
        } catch (Exception e) {e.printStackTrace();}  
        return "";
    }

    public static String stringFromDate(Date date) {
        try {  
            SimpleDateFormat dformatter = new SimpleDateFormat("yyyy-MM-dd");  
            return dformatter.format(date);
        } catch (Exception e) {e.printStackTrace();}  
        return "";
    }
    
}
