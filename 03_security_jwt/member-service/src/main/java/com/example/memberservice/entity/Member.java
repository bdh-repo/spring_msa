package com.example.memberservice.entity;

public class Member {
    private String memberId;
    private String password;
    private String role;
    private String name;
    private String email;

    public Member() {}

    public Member(String memberId, String password, String role, String name, String email) {
        this.memberId = memberId;
        this.password = password;
        this.role = role;
        this.name = name;
        this.email = email;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}