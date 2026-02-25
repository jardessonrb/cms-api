package com.labjb.cms.repository;

import com.labjb.cms.domain.model.Compartilhamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompartilhamentoRepository extends JpaRepository<Compartilhamento, Long> {
    Optional<Compartilhamento> findByCampeonatoId(Long campeonatoId);
    Optional<Compartilhamento> findByToken(String token);
}
