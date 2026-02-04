package com.labjb.cms.repository;

import com.labjb.cms.domain.model.Nota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotaRepository extends JpaRepository<Nota, Long> {

    Optional<Nota> findByUuid(UUID uuid);

    List<Nota> findByRegistroDisputaUuid(UUID registroDisputaUuid);

    boolean existsByJuradoUuidAndRegistroDisputaUuid(UUID juradoUuid, UUID registroDisputaUuid);
}
