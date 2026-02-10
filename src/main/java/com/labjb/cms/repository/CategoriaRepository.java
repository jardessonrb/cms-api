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

    Optional<Categoria> findByUuidAndCampeonatoUuid(UUID categoriaId, UUID campeonatoId);
}
