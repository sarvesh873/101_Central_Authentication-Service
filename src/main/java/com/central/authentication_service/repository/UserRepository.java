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

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    List<User> findByUsername(String username);
    Optional<User> findByUsernameAndEmail(String username, String email);
    Optional<User> findByUserCode(String userCode);
    @Query("SELECT u.role FROM User u WHERE u.userCode = :userCode")
    Optional<Role> getUserRoleByUserCode(@Param("userCode") UUID userCode);
}
