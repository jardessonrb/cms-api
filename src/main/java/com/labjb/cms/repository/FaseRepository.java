package com.labjb.cms.repository;

import com.labjb.cms.domain.dto.out.PontuacaoParcialDto;
import com.labjb.cms.domain.model.Categoria;
import com.labjb.cms.domain.model.Fase;
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
public interface FaseRepository extends JpaRepository<Fase, Long> {
    Optional<Fase> findByUuid(UUID id);
    Page<Fase> findByCategoriaUuidOrderByOrdemDesc(UUID categoriaId, Pageable pageable);
    long countByCategoria(Categoria categoria);

    @Query("SELECT MAX(tf.ordem) FROM Fase tf WHERE tf.categoria.uuid = :categoriaId")
    Long findMaxOrdemByCategoriaUuid(UUID categoriaId);

    @Query(value = """
            with notas_por_rodada as (
            	select tf.nome as fase, tc.nome as categoria,
            	tr.nome, trd.atleta_id, avg(tn.nota_da_dupla) as nota_dupla, avg(tn.nota_do_atleta) as nota_atleta, 
            	(avg(tn.nota_da_dupla) + avg(tn.nota_do_atleta)) as total_por_rodada
            	from tb_nota tn
            	join tb_registro_disputa trd on tn.registro_disputa_id = trd.id
            	join tb_disputa td on trd.disputa_id = td.id
            	join tb_rodada tr on td.rodada_id = tr.id
            	join tb_fase tf on tr.fase_id = tf.id
            	join tb_categoria tc on tf.categoria_id = tc.id
            	where tf.id = :faseId
            	group by tf.nome, tc.nome, tr.nome, trd.atleta_id
            ),
            partidas_por_atleta as (
            	select ta.id as atleta_id, 
            	count(distinct td.id) as partidas,
            	count(case when td.situacao = 'CONCLUIDA' then 1 end) as partidas_concluidas
            	from tb_registro_disputa trd
            	join tb_disputa td on trd.disputa_id = td.id
            	join tb_atleta ta on trd.atleta_id = ta.id
            	where td.rodada_id in (select tr.id from tb_rodada tr where tr.fase_id = :faseId)
            	group by ta.id
            )
            select
            ta.id as atleta_id,
            npr.categoria,
            npr.fase,
            (ta.apelido || ' - '|| ta.grupo) as atleta, 
            ppa.partidas, 
            ppa.partidas_concluidas, 
            sum(npr.nota_dupla) as pontuacao_por_dupla, 
            sum(npr.nota_atleta) as pontuacao_por_atleta, 
            sum(npr.total_por_rodada) as total_fase 
            from notas_por_rodada npr
            join tb_atleta ta on npr.atleta_id = ta.id
            join partidas_por_atleta ppa on ppa.atleta_id = ta.id
            group by ta.id, npr.categoria, npr.fase, ta.apelido, ta.grupo, ppa.partidas, ppa.partidas_concluidas
            order by total_fase desc
            """, 
            nativeQuery = true)
    List<Object[]> findPontuacaoParcialByFaseId(@Param("faseId") Long faseId);
}
