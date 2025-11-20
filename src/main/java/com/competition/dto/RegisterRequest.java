package com.competition.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String realName;
    private String email;
    private String phone;
    private String school;
    private String department;
    private String studentNo;
}
