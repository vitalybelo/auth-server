package com.codetech.authserver.model;

import lombok.*;

@Data
public class LoginRequest {

    private String username;
    private String password;

}
