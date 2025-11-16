package com.central.authentication_service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private double errorCode;
    private String description;
    private String errorType;
    private  String errorMessage;

    public ErrorResponse(double errorCode, String description, String errorType, String errorMessage) {
        this.errorCode = errorCode;
        this.description = description;
        this.errorType = errorType;
        this.errorMessage = errorMessage;
    }

}
