package com.campusconnect.auth.repository;

import com.campusconnect.auth.entity.Role;
import com.campusconnect.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByRole(Role role);

    java.util.List<User> findByRoleInAndStatus(java.util.Collection<Role> roles, com.campusconnect.auth.entity.UserStatus status);

    java.util.List<User> findByRole(Role role);
}
