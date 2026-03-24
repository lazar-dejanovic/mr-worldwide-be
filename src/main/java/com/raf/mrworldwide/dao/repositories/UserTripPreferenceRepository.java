package com.raf.mrworldwide.dao.repositories;

import com.raf.mrworldwide.domain.entities.user.User;
import com.raf.mrworldwide.domain.entities.user.UserTripPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserTripPreferenceRepository extends JpaRepository<UserTripPreference, UUID> {

    Optional<UserTripPreference> findByUser(User user);
}

