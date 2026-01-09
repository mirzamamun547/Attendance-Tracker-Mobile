package com.example.myattendancetracker;

public class Student {

    private int roll;
    private String name;
    private String className;
    private int presentDays;
    private int totalDays;

    // ✅ Field to track CheckBox state
    private boolean present;

    public Student(int roll, String name, String className, int presentDays, int totalDays) {
        this.roll = roll;
        this.name = name;
        this.className = className;
        this.presentDays = presentDays;
        this.totalDays = totalDays;
        this.present = false; // default unchecked
    }

    public int getRoll() { return roll; }
    public String getName() { return name; }
    public String getClassName() { return className; }

    public int getPercentage() {
        if (totalDays == 0) return 0; // avoid division by zero
        return (presentDays * 100) / totalDays;
    }

    public void incrementPresent() {
        presentDays++;
        totalDays++;
    }

    // ✅ Getter and setter for CheckBox
    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }
}
