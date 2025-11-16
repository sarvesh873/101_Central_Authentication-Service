package com.central.authentication_service.service;

import org.openapitools.model.LoginRequest;
import org.openapitools.model.LoginResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {

    public ResponseEntity<LoginResponse> loginUser(LoginRequest loginRequest);

    public ResponseEntity<Void> validateToken(String token);

}
