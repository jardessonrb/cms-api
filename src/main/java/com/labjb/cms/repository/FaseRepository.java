package com.labjb.cms.repository;

import com.labjb.cms.domain.model.Categoria;
import com.labjb.cms.domain.model.Fase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FaseRepository extends JpaRepository<Fase, Long> {
    Optional<Fase> findByUuid(UUID id);
    @Query(value = """
            SELECT f FROM Fase f
            WHERE f.categoria.uuid = :categoriaId
              AND (:filtro IS NULL OR
                   LOWER(f.nome) LIKE LOWER(CONCAT('%', CAST(:filtro AS string), '%')) OR
                   CAST(f.ordem AS string) LIKE CONCAT('%', CAST(:filtro AS string), '%'))
            ORDER BY f.ordem DESC
            """)
    Page<Fase> findByCategoriaUuidOrderByOrdemDesc(@Param("filtro") String filtro, @Param("categoriaId") UUID categoriaId, Pageable pageable);
    long countByCategoria(Categoria categoria);

    @Query("SELECT coalesce(MAX(tf.ordem), 0) FROM Fase tf WHERE tf.categoria.uuid = :categoriaId")
    Long findMaxOrdemByCategoriaUuid(UUID categoriaId);

    @Query(value = """
        with notas_por_rodada as (
        	select tf.nome as fase, tc.nome as categoria,
        	tr.id as rodada_id, trd.atleta_id, avg(tn.nota_da_dupla) as nota_dupla, avg(tn.nota_do_atleta) as nota_atleta,
        	(avg(tn.nota_da_dupla) + avg(tn.nota_do_atleta)) as total_por_rodada
        	from tb_nota tn
        	right join tb_registro_disputa trd on tn.registro_disputa_id = trd.id
        	join tb_disputa td on trd.disputa_id = td.id
        	join tb_rodada tr on td.rodada_id = tr.id and tr.tipo_rodada != 'DESEMPATE'
        	join tb_fase tf on tr.fase_id = tf.id
        	join tb_categoria tc on tf.categoria_id = tc.id
        	where tf.id = :faseId
        	group by  trd.atleta_id, tf.nome, tc.nome, tr.id
        ),
        --select * from notas_por_rodada;
        partidas_por_atleta as (
        	select ta.id as atleta_id,
        	count(distinct td.id) as partidas,
        	count(case when td.situacao = 'CONCLUIDA' then 1 end) as partidas_concluidas
        	from tb_registro_disputa trd
        	join tb_disputa td on trd.disputa_id = td.id
        	join tb_atleta ta on trd.atleta_id = ta.id
        	where td.rodada_id in (select tr.id from tb_rodada tr where tr.fase_id = :faseId and tr.tipo_rodada != 'DESEMPATE')
        	group by ta.id
        )
        --select * from partidas_por_atleta;
        select
        ta.id as atleta_id,
        ta.situacao as situacao,
        npr.categoria,
        npr.fase,
        (ta.apelido || ' - '|| ta.grupo) as atleta,
        ta.numero as numero_competidor,
        ppa.partidas,
        ppa.partidas_concluidas,
        coalesce(sum(npr.nota_dupla), 0) as pontuacao_por_dupla,
        coalesce(sum(npr.nota_atleta), 0) as pontuacao_por_atleta,
        coalesce(sum(npr.total_por_rodada), 0) as total_fase
        from notas_por_rodada npr
        join tb_atleta ta on npr.atleta_id = ta.id
        join partidas_por_atleta ppa on ppa.atleta_id = ta.id
        group by ta.id, npr.categoria, npr.fase, ta.apelido, ta.grupo, ta.numero, ppa.partidas, ppa.partidas_concluidas
        order by total_fase desc;
        """,
        nativeQuery = true)
    List<Object[]> findPontuacaoParcialByFaseId(@Param("faseId") Long faseId);

    @Query("SELECT f FROM Fase f WHERE f.categoria.campeonato.id = :campeonatoId AND f.isCompartilhada = true")
    Optional<Fase> findCompartilhadaPorCampeonatoId(@Param("campeonatoId") Long campeonatoId);

    @Query("SELECT f FROM Fase f WHERE f.categoria.campeonato.id = :campeonatoId")
    List<Fase> findAllByCampeonatoId(@Param("campeonatoId") Long campeonatoId);

    @Modifying
    @Query("UPDATE Fase f SET f.isCompartilhada = false WHERE f.categoria.campeonato.id = :campeonatoId")
    void desabilitarCompartilhamentoPorCampeonatoId(@Param("campeonatoId") Long campeonatoId);
}
