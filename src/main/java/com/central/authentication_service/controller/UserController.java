package com.central.authentication_service.controller;


import com.central.authentication_service.model.CentralRequest;
import com.central.authentication_service.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.openapitools.api.UserApi;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@RestController
@Slf4j
public class UserController implements UserApi {

    @Autowired
    private UserService userService;

    @Override
    public ResponseEntity<UserResponse> createUser(CreateUserRequest createUserRequest) {
        log.info("Entered createUser() with username={}, email={}",
                createUserRequest.getUsername(), createUserRequest.getEmail());

        CentralRequest<CreateUserRequest> request = CentralRequest.<CreateUserRequest>builder()
                .t(createUserRequest).build();


        return userService.createUser(request);
    }

    @Override
    public ResponseEntity<UserResponse> getUserByUserCode(String userCode) {
        log.info("Entered getUserByUserCode() with userCode={}", userCode);

        return userService.getUserByUserCode(userCode);
    }

    @Override
    public ResponseEntity<List<UserResponse>> searchUsers(String username, String email) {
        log.info("Entered searchUsers() with username={}, email={}", username, email);

        return userService.searchUsers(username, email);
    }
}
