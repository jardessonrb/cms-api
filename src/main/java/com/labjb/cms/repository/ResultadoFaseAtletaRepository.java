package com.labjb.cms.repository;

import com.labjb.cms.domain.model.ResultadoFaseAtleta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ResultadoFaseAtletaRepository extends JpaRepository<ResultadoFaseAtleta, Long> {
    List<ResultadoFaseAtleta> findByFaseUuid(UUID faseUuid);
    void deleteByFaseUuid(UUID faseUuid);
}
