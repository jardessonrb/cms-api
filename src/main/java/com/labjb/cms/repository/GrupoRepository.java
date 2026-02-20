package com.labjb.cms.repository;

import com.labjb.cms.domain.model.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GrupoRepository extends JpaRepository<Grupo, Long> {
    
    Optional<Grupo> findByNome(String nome);
    
    boolean existsByNome(String nome);

    Optional<Grupo> findByUuid(UUID grupoUuid);
}
