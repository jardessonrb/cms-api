package com.labjb.cms.service;

import com.labjb.cms.domain.dto.in.DisputaForm;
import com.labjb.cms.domain.dto.out.DisputaDto;
import com.labjb.cms.domain.enums.SituacaoDisputaEnum;
import com.labjb.cms.domain.model.*;
import com.labjb.cms.repository.AtletaRepository;
import com.labjb.cms.repository.DisputaRepository;
import com.labjb.cms.repository.RodadaRepository;
import com.labjb.cms.shared.mapper.DisputaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class DisputaService {

    private final DisputaRepository disputaRepository;
    private final RodadaRepository rodadaRepository;
    private final AtletaRepository atletaRepository;
    private final DisputaMapper disputaMapper;

    public Page<DisputaDto> listarDisputasPorRodada(UUID rodadaId, Pageable pageable) {
        return disputaRepository.findByRodadaUuidOrderByCriadoEmDesc(rodadaId, pageable)
                .map(disputaMapper::toDto);
    }

    public DisputaDto buscarDisputaPorUuid(UUID uuid) {
        Disputa disputa = disputaRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Disputa não encontrada"));
        return disputaMapper.toDto(disputa);
    }

    @Transactional
    public DisputaDto criarDisputaManual(UUID rodadaId, DisputaForm disputaForm) {
        Rodada rodada = rodadaRepository.findByUuid(rodadaId)
                .orElseThrow(() -> new RuntimeException("Rodada não encontrada"));

        Atleta atletaA = atletaRepository.findByUuid(disputaForm.atletaAId())
                .orElseThrow(() -> new RuntimeException("Atleta A não encontrado"));

        Atleta atletaB = atletaRepository.findByUuid(disputaForm.atletaBId())
                .orElseThrow(() -> new RuntimeException("Atleta B não encontrado"));

        Disputa disputa = Disputa.builder()
                .rodada(rodada)
                .situacao(SituacaoDisputaEnum.PENDENTE)
                .build();

        RegistroDisputa registroDisputaA = RegistroDisputa.builder()
                .atleta(atletaA)
                .disputa(disputa)
                .build();

        RegistroDisputa registroDisputaB = RegistroDisputa.builder()
                .atleta(atletaB)
                .disputa(disputa)
                .build();

        disputa.setRegistroDisputas(new HashSet<>(List.of(registroDisputaA, registroDisputaB)));

        Disputa disputaSalva = disputaRepository.save(disputa);
        return disputaMapper.toDto(disputaSalva);
    }
}
