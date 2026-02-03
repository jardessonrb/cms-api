package com.labjb.cms.service;

import com.labjb.cms.domain.dto.in.CampeonatoForm;
import com.labjb.cms.domain.dto.out.CampeonatoDto;
import com.labjb.cms.domain.enums.SituacaoCampeonatoEnum;
import com.labjb.cms.domain.model.Campeonato;
import com.labjb.cms.repository.CampeonatoRepository;
import com.labjb.cms.shared.mapper.CampeonatoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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
}
