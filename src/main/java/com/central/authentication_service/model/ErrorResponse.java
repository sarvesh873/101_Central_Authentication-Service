package com.central.authentication_service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a standardized error response structure for API error handling.
 * This class provides a consistent format for all error responses throughout the application.
 */
@Data
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * Numeric code representing the specific error condition.
     * Follows standard HTTP status codes or custom error codes.
     */
    private double errorCode;

    /**
     * Human-readable description providing more details about the error.
     */
    private String description;

    /**
     * Categorization of the error (e.g., "VALIDATION_ERROR", "AUTHENTICATION_FAILED").
     */
    private String errorType;

    /**
     * A brief message summarizing the error condition.
     */
    private String errorMessage;

    /**
     * Constructs a new ErrorResponse with the specified details.
     *
     * @param errorCode    The numeric code representing the error
     * @param description  Detailed description of the error
     * @param errorType    Category/type of the error
     * @param errorMessage Brief error message
     */
    public ErrorResponse(double errorCode, String description, String errorType, String errorMessage) {
        this.errorCode = errorCode;
        this.description = description;
        this.errorType = errorType;
        this.errorMessage = errorMessage;
    }
}
