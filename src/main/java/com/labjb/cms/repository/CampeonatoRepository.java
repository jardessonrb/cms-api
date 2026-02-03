package com.labjb.cms.repository;

import com.labjb.cms.domain.model.Campeonato;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CampeonatoRepository extends JpaRepository<Campeonato, Long> {
    Optional<Campeonato> findByUuid(UUID id);
    Page<Campeonato> findAllByOrderByCriadoEmDesc(Pageable pageable);
}
