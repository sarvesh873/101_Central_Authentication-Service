package com.central.authentication_service.service;


import com.central.authentication_service.model.CentralRequest;
import org.openapitools.model.CreateUserRequest;
import org.openapitools.model.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


/**
    * This interface specifies all the service methods that must be implemented.
*/

@Service
public interface UserService {

    ResponseEntity<UserResponse> createUser(CentralRequest<CreateUserRequest> request);

}
