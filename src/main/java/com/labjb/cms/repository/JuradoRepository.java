package com.labjb.cms.repository;

import com.labjb.cms.domain.model.Jurado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JuradoRepository extends JpaRepository<Jurado, Long> {

    Optional<Jurado> findByUuid(UUID uuid);

    List<Jurado> findByUuidIn(List<UUID> uuids);

    @Query(value = """
            SELECT j FROM Jurado j
            WHERE j.campeonato.uuid = :campeonatoId
              AND (:filtro IS NULL OR
                   LOWER(j.nome)    LIKE LOWER(CONCAT('%', CAST(:filtro AS string), '%')) OR
                   LOWER(j.apelido) LIKE LOWER(CONCAT('%', CAST(:filtro AS string), '%')) OR
                   LOWER(j.grupo)   LIKE LOWER(CONCAT('%', CAST(:filtro AS string), '%')))
            ORDER BY j.criadoEm DESC
            """)
    Page<Jurado> findAllByCampeonatoWithFilter(@Param("filtro") String filtro, @Param("campeonatoId") UUID campeonatoId, Pageable pageable);
}
