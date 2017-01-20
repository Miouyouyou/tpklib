/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.lib;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.tizen.tpklib.lib.PerformanceData;

public class Performance {
    private static LinkedHashMap<String, PerformanceData> dataList = new LinkedHashMap();
    public static boolean doPerformance = false;

    public static void setStartTime(String subject) {
        PerformanceData pData = Performance.getData(subject);
        if (pData == null) {
            pData = new PerformanceData(subject);
            Performance.addData(pData);
        }
        pData.setStartTime();
    }

    public static boolean setEndTime(String subject) {
        PerformanceData pData = Performance.getData(subject);
        if (pData == null) {
            return false;
        }
        pData.setEndTime();
        return true;
    }

    public static void addData(PerformanceData pData) {
        dataList.put(pData.getSubject(), pData);
    }

    public static PerformanceData getData(String subject) {
        return dataList.get(subject);
    }

    public static Map<String, PerformanceData> getDataList() {
        return dataList;
    }

    public static StringBuffer getAllData() {
        StringBuffer retDataList = new StringBuffer();
        Set<String> keySet = dataList.keySet();
        for (String key : keySet) {
            retDataList.append(dataList.get(key).toString() + "\n");
        }
        return retDataList;
    }

    public static void removeAllData() {
        dataList.clear();
    }
}

