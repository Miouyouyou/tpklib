/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.lib;

public class PerformanceData {
    private String subject = "PerformaceData";
    private long startTime = 0;
    private long endTime = 0;

    public PerformanceData(String subject) {
        this.subject = subject;
    }

    public void setStartTime() {
        this.startTime = System.currentTimeMillis();
    }

    public void setEndTime() {
        this.endTime = System.currentTimeMillis();
    }

    public long getProgressTime() {
        return this.endTime - this.startTime;
    }

    public String getSubject() {
        return this.subject;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(this.subject + " : " + this.getProgressTime());
        return buffer.toString();
    }
}

