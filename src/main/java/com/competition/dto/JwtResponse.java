package com.competition.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Integer userId;
    private String username;
    
    public JwtResponse(String token, Integer userId, String username) {
        this.token = token;
        this.userId = userId;
        this.username = username;
    }
}
