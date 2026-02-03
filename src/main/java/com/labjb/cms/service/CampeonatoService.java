package com.labjb.cms.service;

import com.labjb.cms.domain.dto.in.CampeonatoForm;
import com.labjb.cms.domain.dto.out.CampeonatoDto;
import com.labjb.cms.domain.model.Campeonato;
import com.labjb.cms.repository.CampeonatoRepository;
import com.labjb.cms.shared.mapper.CampeonatoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CampeonatoService {

    private final CampeonatoRepository campeonatoRepository;
    private final CampeonatoMapper campeonatoMapper;

    public CampeonatoDto criaCampeonato(CampeonatoForm campeonatoForm){
        Campeonato campeonato = campeonatoMapper.toEntity(campeonatoForm);
        return campeonatoMapper.toDto(campeonatoRepository.save(campeonato));
    }
}
