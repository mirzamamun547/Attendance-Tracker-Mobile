package com.example.myattendancetracker;

public class AttendanceModel {
    public int totalClasses;
    public int presentCount;

    public AttendanceModel() {}

    public AttendanceModel(int totalClasses, int presentCount) {
        this.totalClasses = totalClasses;
        this.presentCount = presentCount;
    }

    public float getPercentage() {
        if (totalClasses == 0) return 0;
        return (presentCount * 100f) / totalClasses;
    }
}
