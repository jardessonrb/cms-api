package com.labjb.cms.service;

import com.labjb.cms.domain.dto.in.CampeonatoForm;
import com.labjb.cms.domain.dto.out.CampeonatoDto;
import com.labjb.cms.domain.dto.out.CampeonatoDetalhadoDto;
import com.labjb.cms.domain.enums.SituacaoCampeonatoEnum;
import com.labjb.cms.domain.model.Campeonato;
import com.labjb.cms.repository.CampeonatoRepository;
import com.labjb.cms.shared.mapper.CampeonatoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CampeonatoService {

    private final CampeonatoRepository campeonatoRepository;
    private final CampeonatoMapper campeonatoMapper;

    public CampeonatoDto criaCampeonato(CampeonatoForm campeonatoForm){
        Campeonato campeonato = campeonatoMapper.toEntity(campeonatoForm);
        campeonato.setSituacao(SituacaoCampeonatoEnum.CRIADO);
        return campeonatoMapper.toDto(campeonatoRepository.save(campeonato));
    }

    public CampeonatoDto atualizaCampeonato(UUID id, CampeonatoForm campeonatoForm) {
        Campeonato campeonato = campeonatoRepository.findByUuid(id)
                .orElseThrow(() -> new RuntimeException("Campeonato não encontrado"));
        
        campeonato.setNome(campeonatoForm.nome());
        return campeonatoMapper.toDto(campeonatoRepository.save(campeonato));
    }

    public Page<CampeonatoDto> listarCampeonatos(Pageable pageable) {
        return campeonatoRepository.findAllByOrderByCriadoEmDesc(pageable)
                .map(campeonatoMapper::toDto);
    }

    public CampeonatoDetalhadoDto buscarCampeonatoPorUuid(UUID uuid) {
        Campeonato campeonato = campeonatoRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Campeonato não encontrado"));

        List<Object[]> quantidadePorCampeonatoById = campeonatoRepository.findQuantidadePorCampeonatoById(campeonato.getId());
        Object[] valores = quantidadePorCampeonatoById.get(0);

        long quantidadeAtletas = valores != null ? ((Number) valores[1]).longValue() : 0;
        long quantidadeCategorias = valores != null ? ((Number) valores[2]).longValue() : 0;
        long quantidadeJurados = valores != null ? ((Number) valores[3]).longValue() : 0;

        return new CampeonatoDetalhadoDto(
                campeonato.getUuid(),
                campeonato.getNome(),
                campeonato.getSituacao(),
                campeonato.getCriadoEm(),
                quantidadeAtletas,
                quantidadeCategorias,
                quantidadeJurados
        );
    }
}
