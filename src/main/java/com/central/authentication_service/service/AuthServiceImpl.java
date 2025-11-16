package com.central.authentication_service.service;

import com.central.authentication_service.repository.UserRepository;
import com.central.authentication_service.utils.JwtUtil;
import org.openapitools.model.LoginRequest;
import org.openapitools.model.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.central.authentication_service.utils.ServiceUtils.constructLoginResponse;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;


    @Override
    public ResponseEntity<LoginResponse> loginUser(LoginRequest loginRequest) {
        Optional<String> tokenOptional = repository.findByEmail(loginRequest.getEmail())
                .filter(u -> passwordEncoder.matches(loginRequest.getPassword(),
                        u.getPassword()))
                .map(u -> jwtUtil.generateToken(u.getUserCode(), String.valueOf(u.getRole())));


        if (tokenOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = tokenOptional.get();
        LoginResponse response = constructLoginResponse(token);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Override
    public ResponseEntity<Void> validateToken(String token) {
        if(token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return jwtUtil.validateToken(token.substring(7))
                ? ResponseEntity.ok().build()
                : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

}
