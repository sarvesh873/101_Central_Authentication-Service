package com.central.authentication_service.repository;

import com.central.authentication_service.model.Role;
import com.central.authentication_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for {@link User} entities.
 * Provides CRUD operations and custom query methods for user management.
 * Extends JpaRepository for basic CRUD operations and JPA functionality.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    /**
     * Finds a user by their email address.
     *
     * @param email The email address to search for
     * @return An Optional containing the user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Finds all users with the given username.
     *
     * @param username The username to search for
     * @return A list of users with the specified username (can be empty)
     */
    List<User> findByUsername(String username);
    
    /**
     * Finds a user by both username and email.
     *
     * @param username The username to search for
     * @param email The email address to search for
     * @return An Optional containing the user if found, empty otherwise
     */
    Optional<User> findByUsernameAndEmail(String username, String email);
    
    /**
     * Finds a user by their unique user code.
     *
     * @param userCode The unique user code to search for
     * @return An Optional containing the user if found, empty otherwise
     */
    Optional<User> findByUserCode(String userCode);

    /**
     * Checks if a user with the given email address exists.
     *
     * @param email The email address to check
     * @return true if a user with the given email exists, false otherwise
     */
    Boolean existsByEmail(String email);
    
    /**
     * Retrieves the role of a user by their user code.
     * This is a custom query that only fetches the role field for better performance.
     *
     * @param userCode The unique user code
     * @return An Optional containing the user's role if found, empty otherwise
     */
    @Query("SELECT u.role FROM User u WHERE u.userCode = :userCode")
    Optional<Role> getUserRoleByUserCode(@Param("userCode") String userCode);
}
