package com.labjb.cms.repository;

import com.labjb.cms.domain.model.Categoria;
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
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    Optional<Categoria> findByUuid(UUID id);
    List<Categoria> findByCampeonatoUuid(UUID campeonatoId);
    
    @Query("SELECT c FROM Categoria c WHERE c.campeonato.uuid = :campeonatoId " +
           "AND (:nome IS NULL OR LOWER(c.nome) LIKE LOWER(CONCAT('%', CAST(:nome AS string), '%')))")
    Page<Categoria> findByCampeonatoUuidWithFilters(@Param("campeonatoId") UUID campeonatoId, 
                                                   @Param("nome") String nome, 
                                                   Pageable pageable);

    @Query(value = """
            select tc.id, 
                   count(distinct ta.id) as quantidade_atletas, 
                   count(distinct tf.id) as quantidade_fases 
            from tb_categoria tc 
            left join tb_inscricao_categoria tic on tic.categoria_id = tc.id 
            left join tb_atleta ta on tic.atleta_id = ta.id 
            left join tb_fase tf on tf.categoria_id = tc.id 
            where tc.id = :id 
            group by tc.id
            """, nativeQuery = true)
    List<Object[]> findCategoriaWithQuantidadesByUuid(@Param("id") Long id);

    Optional<Categoria> findByUuidAndCampeonatoUuid(UUID categoriaId, UUID campeonatoId);
    
    Optional<Categoria> findByCampeonatoIdAndNomeIgnoreCase(Long campeonatoId, String nome);
    
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
                where tc."uuid" = :categoriaId
                group by  trd.atleta_id, tf.nome, tc.nome, tr.id
            )
            select
            ta.id as atleta_id,
            ta.situacao as situacao,
            npr.categoria,
            (ta.apelido || ' - '|| ta.grupo) as atleta,
            ta.graduacao,
            ta.numero as numero_competidor,
            coalesce(sum(npr.nota_dupla), 0) as pontuacao_por_dupla,
            coalesce(sum(npr.nota_atleta), 0) as pontuacao_por_atleta,
            coalesce(sum(npr.total_por_rodada), 0) as total_geral
            from notas_por_rodada npr
            join tb_atleta ta on npr.atleta_id = ta.id
            group by ta.id, npr.categoria, ta.apelido, ta.grupo, ta.graduacao, ta.numero
            order by total_geral desc
            """, nativeQuery = true)
    List<Object[]> findPontuacaoGeralByCategoriaId(@Param("categoriaId") UUID categoriaId);
}
