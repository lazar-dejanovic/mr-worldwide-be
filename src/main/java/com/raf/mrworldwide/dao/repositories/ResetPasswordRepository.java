package com.raf.mrworldwide.dao.repositories;

import com.raf.mrworldwide.domain.entities.user.ResetPassword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ResetPasswordRepository extends JpaRepository<ResetPassword, UUID> {

}
