package com.fia.project_ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fia.project_ecommerce.model.Role;
 

 
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
 
    Role findByName(String name);
 }