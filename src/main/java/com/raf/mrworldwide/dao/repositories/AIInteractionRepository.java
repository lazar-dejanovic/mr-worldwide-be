package com.raf.mrworldwide.dao.repositories;

import com.raf.mrworldwide.domain.entities.ai.AIInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AIInteractionRepository extends JpaRepository<AIInteraction, UUID> {
}
