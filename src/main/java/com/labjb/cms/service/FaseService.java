package com.labjb.cms.service;

import com.labjb.cms.domain.dto.in.FaseForm;
import com.labjb.cms.domain.dto.out.FaseDto;
import com.labjb.cms.domain.enums.CriterioEntrada;
import com.labjb.cms.domain.enums.SituacaoFaseEnum;
import com.labjb.cms.domain.model.Fase;
import com.labjb.cms.domain.model.Categoria;
import com.labjb.cms.domain.model.Atleta;
import com.labjb.cms.repository.FaseRepository;
import com.labjb.cms.repository.CategoriaRepository;
import com.labjb.cms.repository.AtletaRepository;
import com.labjb.cms.shared.mapper.FaseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class FaseService {

    private final FaseRepository faseRepository;
    private final CategoriaRepository categoriaRepository;
    private final AtletaRepository atletaRepository;
    private final FaseMapper faseMapper;

    public FaseDto criaFase(FaseForm faseForm) {
        Categoria categoria = categoriaRepository.findByUuid(faseForm.categoriaId())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        Fase fase = faseMapper.toEntity(faseForm);
        fase.setCategoria(categoria);
        fase.setSituacao(SituacaoFaseEnum.CRIADA);
        fase.setOrdem(Math.toIntExact(faseRepository.countByCategoria(categoria) + 1));

        // Se critério for TODOS, adicionar todos os atletas da categoria
        if (faseForm.criterioEntrada() == CriterioEntrada.TODOS) {
            List<Atleta> atletasCategoria = atletaRepository.findAllByCategoriaWithFilter(null, faseForm.categoriaId(), null)
                    .getContent();
            fase.setAtletas(atletasCategoria.stream().collect(Collectors.toSet()));
        }

        return faseMapper.toDto(faseRepository.save(fase));
    }

    public FaseDto atualizaFase(UUID id, FaseForm faseForm) {
        Fase fase = faseRepository.findByUuid(id)
                .orElseThrow(() -> new RuntimeException("Fase não encontrada"));

        Categoria categoria = categoriaRepository.findByUuid(faseForm.categoriaId())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        fase.setNome(faseForm.nome());
        fase.setCriterioEntrada(faseForm.criterioEntrada());
        fase.setQuantidadeAtletas(faseForm.quantidadeAtletas());
        fase.setCategoria(categoria);

        return faseMapper.toDto(faseRepository.save(fase));
    }

    public Page<FaseDto> listarFasesPorCategoria(UUID categoriaId, Pageable pageable) {
        return faseRepository.findByCategoriaUuidOrderByOrdemDesc(categoriaId, pageable)
                .map(faseMapper::toDto);
    }
}
