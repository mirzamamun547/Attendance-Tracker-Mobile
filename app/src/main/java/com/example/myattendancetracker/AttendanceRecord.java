package com.example.myattendancetracker;

public class AttendanceRecord {
    private String date;
    private boolean present;
    private boolean hasReason; // true if absence reason submitted
    private String reason;

    public AttendanceRecord(String date, boolean present) {
        this.date = date;
        this.present = present;
        this.hasReason = present; // if present, no reason needed
        this.reason = "";
    }

    public String getDate() { return date; }
    public boolean isPresent() { return present; }

    public boolean hasReason() { return hasReason; }
    public void setHasReason(boolean hasReason) { this.hasReason = hasReason; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
