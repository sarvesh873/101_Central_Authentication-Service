package com.central.authentication_service.service;

import com.central.authentication_service.model.CentralRequest;
import com.central.authentication_service.model.Role;
import com.central.authentication_service.model.User;
import com.central.authentication_service.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.CreateUserRequest;
import org.openapitools.model.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.central.authentication_service.utils.ServiceUtils.constructUserResponse;
import static com.central.authentication_service.utils.UserCodeUtil.generateUserCode;

@Slf4j
@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private UserRepository repository;

    @Autowired
    private  PasswordEncoder passwordEncoder;


    @Override
    public ResponseEntity<UserResponse> createUser(CentralRequest<CreateUserRequest> request) {
        String username = request.getT().getUsername();
        log.info("Inside createUser for username {} and Role {}", username, Role.valueOf(request.getT().getRole()));
        User user = User.builder()
                .userCode(generateUserCode(username))
                .username(username)
                .email(request.getT().getEmail())
                .role(Role.valueOf(request.getT().getRole()))
                .password(passwordEncoder.encode(request.getT().getPassword()))
                .build();

        repository.save(user);
        final UserResponse userResponse = constructUserResponse(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }
}
