package com.labjb.cms.service;

import com.labjb.cms.domain.dto.in.DisputaForm;
import com.labjb.cms.domain.dto.out.DisputaDto;
import com.labjb.cms.domain.enums.SituacaoDisputaEnum;
import com.labjb.cms.domain.enums.TipoDisputaEnum;
import com.labjb.cms.domain.enums.TipoRegistroPontuacaoEnum;
import com.labjb.cms.domain.model.*;
import com.labjb.cms.repository.AtletaRepository;
import com.labjb.cms.repository.DisputaRepository;
import com.labjb.cms.repository.RodadaRepository;
import com.labjb.cms.shared.errors.exception.RegraNegocioException;
import com.labjb.cms.shared.mapper.DisputaMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DisputaService {

    private final DisputaRepository disputaRepository;
    private final RodadaRepository rodadaRepository;
    private final AtletaRepository atletaRepository;
    private final DisputaMapper disputaMapper;

    public List<DisputaDto> listarDisputasPorRodada(UUID rodadaId) {
        return disputaRepository.findByRodadaUuidOrderByCriadoEmDesc(rodadaId)
                .stream().map(disputaMapper::toDto)
                .collect(Collectors.toList());
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

        List<RegistroDisputa> registrosDisputa = disputaForm.atletas().stream().map(atletaForm -> {
            Atleta atleta = atletaRepository
                            .findByUuid(atletaForm.atletaId())
                            .orElseThrow(() -> new EntityNotFoundException("Atleta não encontrado"));

            return RegistroDisputa.builder()
                    .atleta(atleta)
                    .tipoRegistro(atletaForm.tipoRegistroDisputa())
                    .build();
            }
        ).collect(Collectors.toList());

        Disputa disputa = Disputa.builder()
                .rodada(rodada)
                .situacao(SituacaoDisputaEnum.PENDENTE)
                .tipoDisputa(defineTipoDisputaComBaseEmRegistrosDisputa(registrosDisputa))
                .registroDisputas(registrosDisputa.stream().collect(Collectors.toSet()))
                .build();

        return disputaMapper.toDto(disputaRepository.save(disputa));
    }

    public static TipoDisputaEnum defineTipoDisputaComBaseEmRegistrosDisputa(List<RegistroDisputa> registrosDisputa){
        boolean todosRegistrosSemPontuacao = registrosDisputa.stream().allMatch(registroDisputa -> registroDisputa.getTipoRegistro().equals(TipoRegistroPontuacaoEnum.NAO_PONTUADO));
        if(todosRegistrosSemPontuacao){
            throw new RegraNegocioException("Todas as disputas estão marcadas para não pontuada.");
        }

        if(registrosDisputa.size() == 1 && registrosDisputa.get(0).getTipoRegistro().equals(TipoRegistroPontuacaoEnum.PONTUADO)){
            return TipoDisputaEnum.INDIVIDUAL;
        }

        if(registrosDisputa.size() == 2){
            boolean possuiAlgumAtletaComRegistroDisputaParaNaoPontuado = registrosDisputa.stream().anyMatch(registroDisputa -> registroDisputa.getTipoRegistro().equals(TipoRegistroPontuacaoEnum.NAO_PONTUADO));
            if(possuiAlgumAtletaComRegistroDisputaParaNaoPontuado){
                return TipoDisputaEnum.INDIVIDUAL;
            }else{
                return TipoDisputaEnum.DUPLA;
            }
        }

        throw new RegraNegocioException("Tipo de disputa não definido.");
    }
}
