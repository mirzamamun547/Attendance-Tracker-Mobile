package com.example.myattendancetracker;

public class AttendanceModel {
    private int totalClasses;
    private int presentCount;

    // Required empty constructor for Firestore
    public AttendanceModel() {}

    public AttendanceModel(int totalClasses, int presentCount) {
        this.totalClasses = totalClasses;
        this.presentCount = presentCount;
    }

    // Getters and setters (important for Firestore mapping)
    public int getTotalClasses() {
        return totalClasses;
    }

    public void setTotalClasses(int totalClasses) {
        this.totalClasses = totalClasses;
    }

    public int getPresentCount() {
        return presentCount;
    }

    public void setPresentCount(int presentCount) {
        this.presentCount = presentCount;
    }

    public float getPercentage() {
        if (totalClasses == 0) return 0f;
        return (presentCount * 100f) / totalClasses;
    }
}
