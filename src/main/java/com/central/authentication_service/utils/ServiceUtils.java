package com.central.authentication_service.utils;

import com.central.authentication_service.model.User;
import org.openapitools.model.UserResponse;
import org.springframework.beans.BeanUtils;

import java.util.Objects;

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

}
