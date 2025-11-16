package com.central.authentication_service.service;

import com.central.authentication_service.exception.UserDoesNotExistException;
import com.central.authentication_service.model.CentralRequest;
import com.central.authentication_service.model.Role;
import com.central.authentication_service.model.User;
import com.central.authentication_service.repository.UserRepository;
import com.central.authentication_service.utils.ServiceUtils;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.CreateUserRequest;
import org.openapitools.model.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

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

    @Override
    public ResponseEntity<UserResponse> getUserByUserCode(String userCode) {
        final Optional<User> user;
        try {
            user = repository.findByUserCode(userCode);
            if(user.isPresent()) {
                final UserResponse userResponse = constructUserResponse(user);
                return ResponseEntity.ok(userResponse);
            }
            throw new UserDoesNotExistException("User not found with userCode: " + userCode);
        } catch (UserDoesNotExistException e) {
            log.error("User not found with userCode: " + userCode);
            throw e;
        }

    }

    @Override
    public ResponseEntity<List<UserResponse>> searchUsers(String username, String email) {

        if (username == null && email == null) {
            throw new IllegalArgumentException("Either username or email must be provided");
        }

        List<User> users;

        try {
            if (username != null && email != null) {
                // Both username and email provided
                users = repository.findByUsernameAndEmail(username, email)
                        .map(List::of)
                        .orElseThrow(() -> new UserDoesNotExistException(
                                String.format("User not found with username: '%s' and email: '%s'", username, email)
                        ));
            } else if (username != null) {
                // Only username provided
                users = repository.findByUsername(username);
                if (users.isEmpty()) {
                    throw new UserDoesNotExistException(
                            String.format("No users found with username: '%s'", username)
                    );
                }
            } else if (email != null) {
                // Only email provided
                users = repository.findByEmail(email)
                        .map(List::of)
                        .orElseThrow(() -> new UserDoesNotExistException(
                                String.format("User not found with email: '%s'", email)
                        ));
            } else {
                // Neither username nor email provided
                throw new IllegalArgumentException("At least one search parameter (username or email) must be provided");
            }
        } catch (Exception ex) {
            throw new UserDoesNotExistException("An error occurred while searching for users "+ ex.getMessage());
        }

        List<UserResponse> response = users.stream()
                .map(ServiceUtils::constructUserResponse)
                .toList();

        return ResponseEntity.ok(response);
    }



}
