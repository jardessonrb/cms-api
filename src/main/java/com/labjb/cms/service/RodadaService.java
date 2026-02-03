package com.labjb.cms.service;

import com.labjb.cms.domain.dto.in.RodadaForm;
import com.labjb.cms.domain.dto.out.RodadaDto;
import com.labjb.cms.domain.enums.SituacaoRodadaEnum;
import com.labjb.cms.domain.model.Rodada;
import com.labjb.cms.domain.model.Fase;
import com.labjb.cms.repository.RodadaRepository;
import com.labjb.cms.repository.FaseRepository;
import com.labjb.cms.shared.mapper.RodadaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class RodadaService {

    private final RodadaRepository rodadaRepository;
    private final FaseRepository faseRepository;
    private final RodadaMapper rodadaMapper;

    public RodadaDto criaRodada(RodadaForm rodadaForm) {
        Fase fase = faseRepository.findByUuid(rodadaForm.faseId())
                .orElseThrow(() -> new RuntimeException("Fase não encontrada"));

        Rodada rodada = rodadaMapper.toEntity(rodadaForm);
        rodada.setFase(fase);
        rodada.setSituacao(SituacaoRodadaEnum.INICIADA);

        return rodadaMapper.toDto(rodadaRepository.save(rodada));
    }

    public RodadaDto atualizaRodada(UUID id, RodadaForm rodadaForm) {
        Rodada rodada = rodadaRepository.findByUuid(id)
                .orElseThrow(() -> new RuntimeException("Rodada não encontrada"));

        rodada.setNome(rodadaForm.nome());

        return rodadaMapper.toDto(rodadaRepository.save(rodada));
    }

    public Page<RodadaDto> listarRodadasPorFase(UUID faseId, String filtro, Pageable pageable) {
        return rodadaRepository.findByFaseUuidWithFilter(faseId, filtro, pageable)
                .map(rodadaMapper::toDto);
    }

    public RodadaDto finalizarRodada(UUID id) {
        Rodada rodada = rodadaRepository.findByUuid(id)
                .orElseThrow(() -> new RuntimeException("Rodada não encontrada"));

        rodada.setSituacao(SituacaoRodadaEnum.FINALIZADA);

        return rodadaMapper.toDto(rodadaRepository.save(rodada));
    }
}
