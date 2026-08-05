package com.smartretail.pricemonitor.repository;

import com.smartretail.pricemonitor.constants.RoleName;
import com.smartretail.pricemonitor.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
