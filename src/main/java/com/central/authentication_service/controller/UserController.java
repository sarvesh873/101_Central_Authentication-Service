package com.central.authentication_service.controller;


import com.central.authentication_service.model.CentralRequest;
import com.central.authentication_service.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.openapitools.api.UserApi;
import org.springframework.beans.factory.annotation.Autowired;

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

//    @Override
//    public ResponseEntity<User> getUserByUserCode(UUID userCode) {
//        log.info("Entered getUserByUserCode() with userCode={}", userCode);
//
//        User user = userService.getUserByUserCode(userCode);
//        return ResponseEntity.ok(user);
//    }
//
//    @Override
//    public ResponseEntity<GetUserRole200Response> getUserRole(UUID userCode) {
//        log.info("Entered getUserRole() with userCode={}", userCode);
//
//        GetUserRole200Response response = userService.getUserRole(userCode);
//        return ResponseEntity.ok(response);
//    }
//
//    @Override
//    public ResponseEntity<List<User>> searchUsers(String username, String email) {
//        log.info("Entered searchUsers() with username={}, email={}", username, email);
//
//        List<User> users = userService.searchUsers(username, email);
//        return ResponseEntity.ok(users);
//    }
}
