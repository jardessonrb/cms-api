package com.labjb.cms.repository;

import com.labjb.cms.domain.model.Categoria;
import com.labjb.cms.domain.model.Fase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FaseRepository extends JpaRepository<Fase, Long> {
    Optional<Fase> findByUuid(UUID id);
    Page<Fase> findByCategoriaUuidOrderByOrdemDesc(UUID categoriaId, Pageable pageable);
    long countByCategoria(Categoria categoria);
}
