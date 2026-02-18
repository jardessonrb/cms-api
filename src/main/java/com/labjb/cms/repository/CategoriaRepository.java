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

    @Query(value = """
            select tc.id, 
                   count(distinct ta.id) as quantidade_atletas, 
                   count(distinct tf.id) as quantidade_fases 
            from tb_categoria tc 
            left join tb_inscricao_categoria tic on tic.categoria_id = tc.id 
            left join tb_atleta ta on tic.atleta_id = ta.id 
            left join tb_fase tf on tf.categoria_id = tc.id 
            where tc.id = :id 
            group by tc.id
            """, nativeQuery = true)
    List<Object[]> findCategoriaWithQuantidadesByUuid(@Param("id") Long id);

    Optional<Categoria> findByUuidAndCampeonatoUuid(UUID categoriaId, UUID campeonatoId);
    
    Optional<Categoria> findByCampeonatoIdAndNomeIgnoreCase(Long campeonatoId, String nome);
}
