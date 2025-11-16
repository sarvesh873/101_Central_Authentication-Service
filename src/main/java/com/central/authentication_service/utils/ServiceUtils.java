package com.central.authentication_service.utils;

import com.central.authentication_service.model.User;
import org.openapitools.model.LoginResponse;
import org.openapitools.model.UserResponse;
import org.springframework.beans.BeanUtils;

import java.util.Objects;
import java.util.Optional;

public class ServiceUtils {

    public static UserResponse constructUserResponse(User user) {

        if(Objects.isNull(user)){
            throw new IllegalArgumentException("User is null");
        }
        UserResponse userResponse = new UserResponse();
        BeanUtils.copyProperties(user,userResponse);
        userResponse.setRole(UserResponse.RoleEnum.valueOf(user.getRole().toString()));
        return userResponse;
    }

    public static UserResponse constructUserResponse(Optional<User> user) {

        if(Objects.isNull(user)){
            throw new IllegalArgumentException("User is null");
        }
        UserResponse userResponse = new UserResponse();
        BeanUtils.copyProperties(user.get(),userResponse);
        userResponse.setRole(UserResponse.RoleEnum.valueOf(user.get().getRole().toString()));
        return userResponse;
    }

    public static LoginResponse constructLoginResponse(String token) {

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setAccessToken(token);
        loginResponse.setExpiresIn("10 Hrs");
        return loginResponse;
    }

}
