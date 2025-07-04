package com.fia.project_ecommerce.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fia.project_ecommerce.model.User;

@Repository
public interface UserRepository  extends JpaRepository<User, Long>{
    User save(User user);
    List<User> findOneByEmail(String email);
    List<User> findAll();
    User findById(long id);
    void deleteById(long id);
    boolean existsByEmail(String email);
    User findByEmail(String email);
}
