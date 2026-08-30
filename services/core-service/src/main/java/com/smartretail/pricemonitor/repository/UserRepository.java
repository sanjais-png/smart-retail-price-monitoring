package com.smartretail.pricemonitor.repository;

import com.smartretail.pricemonitor.constants.ApprovalStatus;
import com.smartretail.pricemonitor.constants.RoleName;
import com.smartretail.pricemonitor.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameOrEmail(String username, String email);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);

    Page<User> findByApprovalStatus(ApprovalStatus approvalStatus, Pageable pageable);
    Page<User> findByRoles_Name(RoleName roleName, Pageable pageable);
    long countByApprovalStatus(ApprovalStatus approvalStatus);
    long countByRoles_Name(RoleName roleName);
}
