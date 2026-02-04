package com.labjb.cms.repository;

import com.labjb.cms.domain.model.RegistroDisputa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RegistroDisputaRepository extends JpaRepository<RegistroDisputa, Long> {

    Optional<RegistroDisputa> findByUuid(UUID uuid);
}
