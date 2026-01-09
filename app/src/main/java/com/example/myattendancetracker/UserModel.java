package com.example.myattendancetracker;

public class UserModel {
    public String email;
    public String role;
    public String password; // for students, auto-generated

    public UserModel() {}

    public UserModel(String email, String role, String password) {
        this.email = email;
        this.role = role;
        this.password = password;
    }
}
