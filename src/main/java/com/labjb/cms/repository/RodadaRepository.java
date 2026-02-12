package com.labjb.cms.repository;

import com.labjb.cms.domain.model.Rodada;
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
public interface RodadaRepository extends JpaRepository<Rodada, Long> {
    Optional<Rodada> findByUuid(UUID id);

    @Query("SELECT r FROM Rodada r WHERE r.fase.uuid = :faseId ORDER BY r.criadoEm DESC")
    Page<Rodada> findByFaseUuidOrderByCriadoEmDesc(UUID faseId, Pageable pageable);

    @Query("SELECT r FROM Rodada r WHERE r.fase.uuid = :faseId AND (:filtro IS NULL OR LOWER(r.nome) LIKE LOWER(CONCAT('%', CAST(:filtro AS string), '%'))) ORDER BY r.criadoEm DESC")
    Page<Rodada> findByFaseUuidWithFilter(@Param("faseId") UUID faseId, @Param("filtro") String filtro, Pageable pageable);

    @Query("SELECT COUNT(r) FROM Rodada r WHERE r.fase.uuid = :faseId AND r.situacao != 'FINALIZADA'")
    Long countRodadasNaoFinalizadasByFaseUuid(@Param("faseId") UUID faseId);

    @Query(value = """
            select tr.id,
            count(case when td.situacao = 'CONCLUIDA' then 1 end) as disputas_concluidas,
            count(case when td.situacao = 'PENDENTE' then 1 end) as disputas_pendentes
            from tb_rodada tr
            join tb_disputa td on td.rodada_id = tr.id
            where tr.id = :rodadaId
            group by tr.id
            """, nativeQuery = true)
    List<Object[]> findDisputasCountByRodadaId(@Param("rodadaId") Long rodadaId);
}
