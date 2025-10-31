package com.sgl.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sgl.backend.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByCodeAndRole_Name(String code, String roleName);
}
