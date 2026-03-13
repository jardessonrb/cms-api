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

    @Query(value = """
            with disputas as (
            select tc.nome as categoria, tr.nome as rodada, tf.nome as fase, td.id, td.situacao from tb_fase tf
            join tb_categoria tc on tf.categoria_id = tc.id
            join tb_rodada tr on tr.fase_id = tf.id
            join tb_disputa td on td.rodada_id = tr.id
            where tf.id = :faseId),

            registros_disputas as (
            select d.id, d.categoria, d.rodada, d.fase, max(trd.atleta_id) as atleta_left, min(trd.atleta_id) as atleta_light from disputas d
            left join tb_registro_disputa trd on trd.disputa_id = d.id
            group by d.id, d.categoria, d.rodada, d.fase)

            select 
            rd.categoria,
            rd.fase,
            td.tipo_disputa,
            td.situacao, 
            rd.rodada,
            (ta.numero || '- ' || ta.apelido || ' - ' || '(' || ta.nome ||')') as atleta_1, 
            (ta2.numero || '- ' || ta2.apelido || ' - ' || '(' || ta2.nome ||')') as atleta_2 
            from registros_disputas rd
            join tb_disputa td on rd.id = td.id
            left join tb_atleta ta on rd.atleta_left = ta.id
            left join tb_atleta ta2 on rd.atleta_light = ta2.id and (atleta_left != atleta_light)
            order by rd.rodada asc
            """, nativeQuery = true)
    List<Object[]> findDisputasPorFase(@Param("faseId") Long faseId);
}
