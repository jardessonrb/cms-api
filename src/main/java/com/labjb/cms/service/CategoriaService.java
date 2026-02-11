package com.labjb.cms.service;

import com.labjb.cms.domain.dto.in.CategoriaForm;
import com.labjb.cms.domain.dto.out.CategoriaDto;
import com.labjb.cms.domain.enums.SituacaoCategoriaEnum;
import com.labjb.cms.domain.enums.SituacaoInscricaoCategoriaEnum;
import com.labjb.cms.domain.model.Categoria;
import com.labjb.cms.domain.model.Campeonato;
import com.labjb.cms.domain.model.InscricaoCategoria;
import com.labjb.cms.domain.model.Atleta;
import com.labjb.cms.repository.CategoriaRepository;
import com.labjb.cms.repository.CampeonatoRepository;
import com.labjb.cms.repository.InscricaoCategoriaRepository;
import com.labjb.cms.repository.AtletaRepository;
import com.labjb.cms.shared.mapper.CategoriaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final CampeonatoRepository campeonatoRepository;
    private final InscricaoCategoriaRepository inscricaoCategoriaRepository;
    private final AtletaRepository atletaRepository;
    private final CategoriaMapper categoriaMapper;

    public CategoriaDto criaCategoria(CategoriaForm categoriaForm) {
        Campeonato campeonato = campeonatoRepository.findByUuid(categoriaForm.campeonatoId())
                .orElseThrow(() -> new RuntimeException("Campeonato não encontrado"));

        Categoria categoria = categoriaMapper.toEntity(categoriaForm);
        categoria.setCampeonato(campeonato);
        categoria.setSituacao(SituacaoCategoriaEnum.CRIADA);

        return categoriaMapper.toDto(categoriaRepository.save(categoria));
    }

    public CategoriaDto atualizaCategoria(UUID id, CategoriaForm categoriaForm) {
        Categoria categoria = categoriaRepository.findByUuid(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        Campeonato campeonato = campeonatoRepository.findByUuid(categoriaForm.campeonatoId())
                .orElseThrow(() -> new RuntimeException("Campeonato não encontrado"));

        categoria.setNome(categoriaForm.nome());
        categoria.setCampeonato(campeonato);

        return categoriaMapper.toDto(categoriaRepository.save(categoria));
    }

    public CategoriaDto buscaCategoriaPorId(UUID id) {
        Categoria categoria = categoriaRepository.findByUuid(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        // Buscar quantidades adicionais
        List<Object[]> quantidades = categoriaRepository.findCategoriaWithQuantidadesByUuid(categoria.getId());
        
        Integer quantidadeAtletas = !quantidades.isEmpty() ? ((Number)quantidades.get(0)[1]).intValue() : 0;
        Integer quantidadeFases = !quantidades.isEmpty() ? ((Number)quantidades.get(0)[2]).intValue() : 0;

        return new CategoriaDto(
            categoria.getUuid(),
            categoria.getNome(),
            categoria.getSituacao(),
            categoria.getCriadoEm(),
            categoria.getCampeonato().getUuid(),
            quantidadeAtletas,
            quantidadeFases
        );
    }

    public Page<CategoriaDto> listarCategoriasPorCampeonato(UUID campeonatoId, String nome, Pageable pageable) {
        return categoriaRepository.findByCampeonatoUuidWithFilters(campeonatoId, nome, pageable)
                .map(categoriaMapper::toDto);
    }
    
    public List<CategoriaDto> listarCategoriasPorCampeonato(UUID campeonatoId) {
        return categoriaRepository.findByCampeonatoUuid(campeonatoId)
                .stream()
                .map(categoriaMapper::toDto)
                .toList();
    }

    public void inscreveAtletaEmCategoria(UUID categoriaId, UUID atletaId) {
        Categoria categoria = categoriaRepository.findByUuid(categoriaId)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        Atleta atleta = atletaRepository.findByUuid(atletaId)
                .orElseThrow(() -> new RuntimeException("Atleta não encontrado"));

        // Verificar se o atleta pertence ao mesmo campeonato da categoria
        if (!atleta.getCampeonato().getUuid().equals(categoria.getCampeonato().getUuid())) {
            throw new RuntimeException("Atleta não pertence ao mesmo campeonato da categoria");
        }

        // Verificar se já existe inscrição
        if (inscricaoCategoriaRepository.findByAtletaAndCategoria(atletaId, categoriaId).isPresent()) {
            throw new RuntimeException("Atleta já inscrito nesta categoria");
        }

        // Criar inscrição
        InscricaoCategoria inscricao = InscricaoCategoria.builder()
                .atleta(atleta)
                .categoria(categoria)
                .situacao(SituacaoInscricaoCategoriaEnum.ATIVO)
                .build();

        inscricaoCategoriaRepository.save(inscricao);
    }

    public void removeInscricaoDeAtletaEmCategoria(UUID categoriaId, UUID atletaId) {
        // Verificar se existe inscrição
        Optional<InscricaoCategoria> inscricaoCategoria = inscricaoCategoriaRepository.findByAtletaAndCategoria(atletaId, categoriaId);
        if (inscricaoCategoria.isEmpty()) {
            throw new RuntimeException("Inscrição não encontrada");
        }

        // Remover inscrição
        inscricaoCategoriaRepository.delete(inscricaoCategoria.get());
    }
}
