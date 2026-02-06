package com.labjb.cms.repository;

import com.labjb.cms.domain.model.ResultadoFaseAtleta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ResultadoFaseAtletaRepository extends JpaRepository<ResultadoFaseAtleta, Long> {
    List<ResultadoFaseAtleta> findByFaseUuid(UUID faseUuid);
    void deleteByFaseUuid(UUID faseUuid);
    
    @Query("SELECT r FROM ResultadoFaseAtleta r WHERE r.fase.uuid = :faseUuid ORDER BY r.total DESC")
    List<ResultadoFaseAtleta> findByFaseUuidOrderByTotalDesc(@Param("faseUuid") UUID faseUuid);
    
    @Query("SELECT COUNT(r) FROM ResultadoFaseAtleta r WHERE r.fase.uuid = :faseUuid AND r.posicao = :posicao")
    Long countByFaseUuidAndPosicao(@Param("faseUuid") UUID faseUuid, @Param("posicao") Integer posicao);
}
