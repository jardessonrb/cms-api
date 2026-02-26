package com.labjb.cms.repository;

import com.labjb.cms.domain.enums.SituacaoAtletaEnum;
import com.labjb.cms.domain.model.Atleta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
              AND (:situacao IS NULL OR a.situacao = :situacao)
              AND (:filtro IS NULL OR
                   LOWER(a.nome)     LIKE LOWER(CONCAT('%', CAST(:filtro AS string), '%')) OR
                   LOWER(a.apelido)  LIKE LOWER(CONCAT('%', CAST(:filtro AS string), '%')) OR
                   LOWER(a.responsavel)  LIKE LOWER(CONCAT('%', CAST(:filtro AS string), '%')) OR
                   LOWER(a.graduacao)LIKE LOWER(CONCAT('%', CAST(:filtro AS string), '%')) OR
                   CAST(a.numero AS string) LIKE CONCAT('%', CAST(:filtro AS string), '%'))
            ORDER BY a.numero ASC, a.apelido ASC, a.nome ASC
            """)
    Page<Atleta> findAllWithFilter(@Param("filtro") String filtro, @Param("campeonatoId") UUID campeonatoId, @Param("situacao") SituacaoAtletaEnum situacao, Pageable pageable);

    @Query(value = """
            SELECT a FROM Atleta a
            JOIN a.inscricoesCategoria ic
            WHERE ic.categoria.uuid = :categoriaId
              AND (:situacao IS NULL OR a.situacao = :situacao)
              AND (:filtro IS NULL OR
                   LOWER(a.nome)     LIKE LOWER(CONCAT('%', CAST(:filtro AS string), '%')) OR
                   LOWER(a.apelido)  LIKE LOWER(CONCAT('%', CAST(:filtro AS string), '%')) OR
                   LOWER(a.responsavel)  LIKE LOWER(CONCAT('%', CAST(:filtro AS string), '%')) OR
                   LOWER(a.graduacao)LIKE LOWER(CONCAT('%', CAST(:filtro AS string), '%')) OR
                   CAST(a.numero AS string) LIKE CONCAT('%', CAST(:filtro AS string), '%'))
            ORDER BY a.numero ASC, a.apelido ASC, a.nome ASC
            """)
    Page<Atleta> findAllByCategoriaWithFilter(@Param("filtro") String filtro, @Param("categoriaId") UUID categoriaId, @Param("situacao") SituacaoAtletaEnum situacao, Pageable pageable);

    @Query(value = """
            SELECT a FROM Atleta a
            JOIN a.fases f
            WHERE f.uuid = :faseId
              AND (:situacao IS NULL OR a.situacao = :situacao)
              AND (:filtro IS NULL OR
                   LOWER(a.nome)     LIKE LOWER(CONCAT('%', CAST(:filtro AS string), '%')) OR
                   LOWER(a.apelido)  LIKE LOWER(CONCAT('%', CAST(:filtro AS string), '%')) OR
                   LOWER(a.responsavel)  LIKE LOWER(CONCAT('%', CAST(:filtro AS string), '%')) OR
                   LOWER(a.graduacao)LIKE LOWER(CONCAT('%', CAST(:filtro AS string), '%')) OR
                   CAST(a.numero AS string) LIKE CONCAT('%', CAST(:filtro AS string), '%'))
            ORDER BY a.numero ASC, a.apelido ASC, a.nome ASC
            """)
    Page<Atleta> findAllByFaseWithFilter(@Param("filtro") String filtro, @Param("faseId") UUID faseId, @Param("situacao") SituacaoAtletaEnum situacao, Pageable pageable);
    
    @Query("SELECT coalesce(MAX(a.numero), 0) FROM Atleta a WHERE a.campeonato.uuid = :campeonatoId")
    Integer findMaxNumeroByCampeonatoUuid(@Param("campeonatoId") UUID campeonatoId);

    @Query("SELECT COUNT(a) > 0 FROM Atleta a JOIN a.inscricoesCategoria ic WHERE ic.categoria.uuid = :categoriaId AND a.uuid = :atletaId")
    boolean existsAtletaInCategoria(@Param("atletaId") UUID atletaId, @Param("categoriaId") UUID categoriaId);

    @Modifying
    @Query(value = """
        with registro_disputas_por_atleta as (
        	select td.id as disputa_id, td.situacao as disputa_situacao, td.tipo_disputa as tipo_disputa, trd.id as reg_disputa_id from tb_registro_disputa trd
        	join tb_disputa td on trd.disputa_id = td.id
        	join tb_atleta ta on trd.atleta_id = ta.id
        	where ta.id = :atletaId
        )
        update tb_registro_disputa trd set tipo_registro = 'NAO_PONTUADO'
        from registro_disputas_por_atleta dpa
        where dpa.reg_disputa_id = trd.id and dpa.disputa_situacao = 'PENDENTE';
    """, nativeQuery = true)
    void atualizaRegistroDisputaParaNaoPontuado(@Param("atletaId") Long atletaId);

    @Modifying
    @Query(value = """
       with disputas_por_atleta as (
            select td.id as disputa_id, count(case when trd.tipo_registro = 'NAO_PONTUADO' then 1 end) as disputas_nao_pontuadas,
            count(distinct trd.id) as disputas from tb_disputa td\s
            join tb_registro_disputa trd on trd.disputa_id = td.id
            where td.id in (select distinct trd2.disputa_id from tb_registro_disputa trd2 where trd2.atleta_id  = :atletaId)
            group by td.id
       )
       update tb_disputa td set
       tipo_disputa = (case when (dpa.disputas_nao_pontuadas = 1 and dpa.disputas = 2) or 
                        (dpa.disputas_nao_pontuadas = 0 and dpa.disputas = 1) then 'INDIVIDUAL' end
        ),
       situacao = (case when dpa.disputas_nao_pontuadas = dpa.disputas then 'CANCELADA' else td.situacao end)
       from disputas_por_atleta dpa where dpa.disputa_id = td.id and td.situacao = 'PENDENTE'
    """, nativeQuery = true)
    void atualizaDisputaConformeTipoRegistroDisputa(@Param("atletaId") Long atletaId);
}
