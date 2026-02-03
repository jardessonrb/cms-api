package com.labjb.cms.service;

import com.labjb.cms.domain.dto.in.CategoriaForm;
import com.labjb.cms.domain.dto.out.CategoriaDto;
import com.labjb.cms.domain.enums.SituacaoCategoriaEnum;
import com.labjb.cms.domain.model.Categoria;
import com.labjb.cms.domain.model.Campeonato;
import com.labjb.cms.repository.CategoriaRepository;
import com.labjb.cms.repository.CampeonatoRepository;
import com.labjb.cms.shared.mapper.CategoriaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final CampeonatoRepository campeonatoRepository;
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

    public List<CategoriaDto> listarCategoriasPorCampeonato(UUID campeonatoId) {
        return categoriaRepository.findByCampeonatoUuid(campeonatoId)
                .stream()
                .map(categoriaMapper::toDto)
                .toList();
    }
}
