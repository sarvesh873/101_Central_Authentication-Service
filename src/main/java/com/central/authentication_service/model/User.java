package com.central.authentication_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Entity class representing a user in the authentication system.
 * Maps to the 'central_users' table in the database.
 * Uses Lombok annotations to reduce boilerplate code.
 */
@Entity
@Table(name = "central_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /**
     * Unique identifier for the user.
     * Automatically generated and used as the primary key in the database.
     */
    @Id
    @Column(name = "user_code", updatable = false, nullable = false)
    private String userCode;

    /**
     * The username of the user.
     * Must be unique within the system.
     */
    @Column(name = "username", nullable = false)
    private String username;

    /**
     * The email address of the user.
     * Used for authentication and communication.
     */
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    /**
     * The hashed password of the user.
     * Never stored in plain text for security reasons.
     */
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * The role assigned to the user.
     * Determines the user's permissions and access levels.
     */
    @Column(name = "role", nullable = false)
    private Role role;
}
