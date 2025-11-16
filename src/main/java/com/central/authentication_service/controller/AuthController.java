package com.central.authentication_service.controller;

import com.central.authentication_service.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.api.AuthApi;
import org.openapitools.model.LoginRequest;
import org.openapitools.model.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class AuthController implements AuthApi {

    @Autowired
    private AuthService authService;

    @Override
    public ResponseEntity<LoginResponse> loginUser(LoginRequest loginRequest) {
        return authService.loginUser(loginRequest);
    }

    @Override
    public ResponseEntity<Void> validateToken(String token) {
        return authService.validateToken(token);
    }

}
