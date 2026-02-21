package com.labjb.cms.repository;

import com.labjb.cms.domain.model.Campeonato;
import com.labjb.cms.domain.model.Grupo;
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
public interface CampeonatoRepository extends JpaRepository<Campeonato, Long> {
    Optional<Campeonato> findByUuid(UUID id);
    Page<Campeonato> findAllByOrderByCriadoEmDesc(Pageable pageable);
    Page<Campeonato> findByGrupoOrderByCriadoEmDesc(Grupo grupo, Pageable pageable);
    
    @Query(value = """
        SELECT tc.id,
        COUNT(DISTINCT ta.id) as quantidade_atletas,
        COUNT(DISTINCT tc2.id) as quantidade_categorias,
        COUNT(DISTINCT tj.id) as quantidade_jurados
        FROM tb_campeonato tc
        LEFT JOIN tb_atleta ta ON ta.campeonato_id = tc.id
        LEFT JOIN tb_categoria tc2 ON tc2.campeonato_id = tc.id
        LEFT JOIN tb_jurado tj ON tj.campeonato_id = tc.id
        WHERE tc.id = :campeonatoId
        GROUP BY tc.id""", nativeQuery = true)
    List<Object[]> findQuantidadePorCampeonatoById(@Param("campeonatoId") Long campeonatoId);
}
