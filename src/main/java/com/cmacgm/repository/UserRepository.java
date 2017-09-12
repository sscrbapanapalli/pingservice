package com.cmacgm.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cmacgm.model.Users;

public interface UserRepository extends JpaRepository<Users, Long>{

}
