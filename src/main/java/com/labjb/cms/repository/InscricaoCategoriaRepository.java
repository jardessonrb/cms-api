package com.labjb.cms.repository;

import com.labjb.cms.domain.model.InscricaoCategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InscricaoCategoriaRepository extends JpaRepository<InscricaoCategoria, Long> {
    
    @Query("SELECT ic FROM InscricaoCategoria ic WHERE ic.atleta.uuid = :atletaId AND ic.categoria.uuid = :categoriaId")
    Optional<InscricaoCategoria> findByAtletaAndCategoria(@Param("atletaId") UUID atletaId, @Param("categoriaId") UUID categoriaId);
    
    void deleteByAtletaUuidAndCategoriaUuid(UUID atletaId, UUID categoriaId);
}
