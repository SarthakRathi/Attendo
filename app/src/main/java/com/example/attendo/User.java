package com.example.attendo;

public class User {
    public String username;
    public String email;
    public String skill;
    public String last_edited;

    // Required no-argument constructor for Firebase
    public User() {}

    // Constructor without password
    public User(String username, String email, String skill, String last_edited) {
        this.username = username;
        this.email = email;
        this.skill = skill;
        this.last_edited = last_edited;
    }
}
