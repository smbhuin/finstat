package com.smbhuin.finstat;

import java.util.Map;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class StatementModel {

    @SerializedName("id") public String id;
    @SerializedName("bank") public String bank;
    @SerializedName("account_type") public String accountType;
    @SerializedName("date_formats") public List<String> dateFormats;
    @SerializedName("locale") public String locale;
    @SerializedName("start_line") public String startLine;
    @SerializedName("end_line") public String endLine;
    @SerializedName("ignore_lines") public List<String> ignoreLines;
    @SerializedName("strategy") public String strategy;
    @SerializedName("record_top_aligned") public Boolean recordTopAligned;
    @SerializedName("area") public List<Float> area;
    @SerializedName("columns") public List<Float> columns;
    @SerializedName("column_indices") public Map<String, Integer> columnIndices;

    public int minDateLength() {
        int minLen = 99;
        for (String df : dateFormats) {
            if (df.length() < minLen) {
                minLen = df.length();
            }
        }
        return minLen;
    }

    public String toString() {
        return bank + "-" + accountType;
    }

}
