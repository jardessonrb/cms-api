package com.labjb.cms.repository;

import com.labjb.cms.domain.model.Atleta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AtletaRepository extends JpaRepository<Atleta, Long> {
    Optional<Atleta> findByUuid(UUID id);
    Optional<Atleta> findByNumeroAndCampeonatoUuid(Integer numero, UUID campeonatoId);

    @Query(value = """
            SELECT a FROM Atleta a
            WHERE a.campeonato.uuid = :campeonatoId
              AND (:filtro IS NULL OR
                   LOWER(a.nome)     LIKE LOWER(CONCAT('%', CAST(:filtro AS string), '%')) OR
                   LOWER(a.apelido)  LIKE LOWER(CONCAT('%', CAST(:filtro AS string), '%')) OR
                   LOWER(a.responsavel)  LIKE LOWER(CONCAT('%', CAST(:filtro AS string), '%')) OR
                   LOWER(a.graduacao)LIKE LOWER(CONCAT('%', CAST(:filtro AS string), '%')) OR
                   CAST(a.numero AS string) LIKE CONCAT('%', CAST(:filtro AS string), '%'))
            ORDER BY a.numero ASC, a.apelido ASC, a.nome ASC
            """)
    Page<Atleta> findAllWithFilter(@Param("filtro") String filtro, @Param("campeonatoId") UUID campeonatoId, Pageable pageable);

    @Query(value = """
            SELECT a FROM Atleta a
            JOIN a.inscricoesCategoria ic
            WHERE ic.categoria.uuid = :categoriaId
              AND (:filtro IS NULL OR
                   LOWER(a.nome)     LIKE LOWER(CONCAT('%', CAST(:filtro AS string), '%')) OR
                   LOWER(a.apelido)  LIKE LOWER(CONCAT('%', CAST(:filtro AS string), '%')) OR
                   LOWER(a.responsavel)  LIKE LOWER(CONCAT('%', CAST(:filtro AS string), '%')) OR
                   LOWER(a.graduacao)LIKE LOWER(CONCAT('%', CAST(:filtro AS string), '%')) OR
                   CAST(a.numero AS string) LIKE CONCAT('%', CAST(:filtro AS string), '%'))
            ORDER BY a.numero ASC, a.apelido ASC, a.nome ASC
            """)
    Page<Atleta> findAllByCategoriaWithFilter(@Param("filtro") String filtro, @Param("categoriaId") UUID categoriaId, Pageable pageable);

    @Query(value = """
            SELECT a FROM Atleta a
            JOIN a.fases f
            WHERE f.uuid = :faseId
              AND (:filtro IS NULL OR
                   LOWER(a.nome)     LIKE LOWER(CONCAT('%', CAST(:filtro AS string), '%')) OR
                   LOWER(a.apelido)  LIKE LOWER(CONCAT('%', CAST(:filtro AS string), '%')) OR
                   LOWER(a.responsavel)  LIKE LOWER(CONCAT('%', CAST(:filtro AS string), '%')) OR
                   LOWER(a.graduacao)LIKE LOWER(CONCAT('%', CAST(:filtro AS string), '%')) OR
                   CAST(a.numero AS string) LIKE CONCAT('%', CAST(:filtro AS string), '%'))
            ORDER BY a.numero ASC, a.apelido ASC, a.nome ASC
            """)
    Page<Atleta> findAllByFaseWithFilter(@Param("filtro") String filtro, @Param("faseId") UUID faseId, Pageable pageable);
    
    @Query("SELECT coalesce(MAX(a.numero), 0) FROM Atleta a WHERE a.campeonato.uuid = :campeonatoId")
    Integer findMaxNumeroByCampeonatoUuid(@Param("campeonatoId") UUID campeonatoId);
}
