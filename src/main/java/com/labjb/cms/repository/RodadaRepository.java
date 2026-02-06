package com.labjb.cms.repository;

import com.labjb.cms.domain.model.Rodada;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RodadaRepository extends JpaRepository<Rodada, Long> {
    Optional<Rodada> findByUuid(UUID id);

    @Query("SELECT r FROM Rodada r WHERE r.fase.uuid = :faseId ORDER BY r.criadoEm DESC")
    Page<Rodada> findByFaseUuidOrderByCriadoEmDesc(UUID faseId, Pageable pageable);

    @Query("SELECT r FROM Rodada r WHERE r.fase.uuid = :faseId AND (:filtro IS NULL OR LOWER(r.fase.nome) LIKE LOWER(CONCAT('%', CAST(:filtro AS string), '%'))) ORDER BY r.criadoEm DESC")
    Page<Rodada> findByFaseUuidWithFilter(@Param("faseId") UUID faseId, @Param("filtro") String filtro, Pageable pageable);

    @Query("SELECT COUNT(r) FROM Rodada r WHERE r.fase.uuid = :faseId AND r.situacao != 'FINALIZADA'")
    Long countRodadasNaoFinalizadasByFaseUuid(@Param("faseId") UUID faseId);
}
