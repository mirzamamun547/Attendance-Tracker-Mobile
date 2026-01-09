package com.example.myattendancetracker;

public class Student {

    // 🔑 Firestore document ID
    private String id;

    private int roll;
    private String name;
    private String className;
    private int presentDays;
    private int totalDays;

    // For attendance checkbox
    private boolean present;

    // 🔹 REQUIRED empty constructor for Firestore
    public Student() {}

    public Student(String id, int roll, String name, String className,
                   int presentDays, int totalDays) {
        this.id = id;
        this.roll = roll;
        this.name = name;
        this.className = className;
        this.presentDays = presentDays;
        this.totalDays = totalDays;
        this.present = false;
    }

    // -------- Getters --------
    public String getId() { return id; }
    public int getRoll() { return roll; }
    public String getName() { return name; }
    public String getClassName() { return className; }
    public int getPresentDays() { return presentDays; }
    public int getTotalDays() { return totalDays; }

    // -------- Attendance logic --------
    public int getPercentage() {
        if (totalDays == 0) return 0;
        return (presentDays * 100) / totalDays;
    }

    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }

    public void markAttendance() {
        if (present) {
            presentDays++;
        }
        totalDays++;
    }
}
