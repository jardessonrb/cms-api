package com.labjb.cms.repository;

import com.labjb.cms.domain.model.Disputa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DisputaRepository extends JpaRepository<Disputa, Long> {

    Optional<Disputa> findByUuid(UUID uuid);

    @Query("SELECT d FROM Disputa d WHERE d.rodada.uuid = :rodadaUuid ORDER BY d.criadoEm DESC")
    Page<Disputa> findByRodadaUuidOrderByCriadoEmDesc(@Param("rodadaUuid") UUID rodadaUuid, Pageable pageable);
}
