package com.example.myattendancetracker;

public class AbsenceModel {

    public String className;
    public String studentEmail;
    public String reason;
    public long timestamp;

    public AbsenceModel(String className, String studentEmail, String reason, long timestamp) {
        this.className = className;
        this.studentEmail = studentEmail;
        this.reason = reason;
        this.timestamp = timestamp;
    }
}
