package com.labjb.cms.repository;

import com.labjb.cms.domain.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    Optional<Categoria> findByUuid(UUID id);
    List<Categoria> findByCampeonatoUuid(UUID campeonatoId);
}
