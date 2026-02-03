package com.labjb.cms.service;

import com.labjb.cms.component.GeradorDeDisputas;
import com.labjb.cms.domain.dto.in.RodadaForm;
import com.labjb.cms.domain.dto.out.RodadaDto;
import com.labjb.cms.domain.enums.SituacaoDisputaEnum;
import com.labjb.cms.domain.enums.SituacaoRodadaEnum;
import com.labjb.cms.domain.model.*;
import com.labjb.cms.repository.AtletaRepository;
import com.labjb.cms.repository.FaseRepository;
import com.labjb.cms.repository.RodadaRepository;
import com.labjb.cms.shared.errors.exception.RegraNegocioException;
import com.labjb.cms.shared.mapper.RodadaMapper;
import com.labjb.cms.shared.utils.Utils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@RequiredArgsConstructor
@Service
public class RodadaService {

    private final RodadaRepository rodadaRepository;
    private final FaseRepository faseRepository;
    private final AtletaRepository atletaRepository;
    private final RodadaMapper rodadaMapper;
    private final GeradorDeDisputas geradorDeDisputas;

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

    @Transactional
    public List<RodadaDto> geraRodadasParaFase(UUID faseId, List<RodadaForm> rodadasForms) {
        Fase fase = faseRepository.findByUuid(faseId)
                .orElseThrow(() -> new RuntimeException("Fase não encontrada"));

        // Verificar se já existem rodadas para esta fase
//        Page<Rodada> rodadasExistentes = rodadaRepository.findByFaseUuidOrderByCriadoEmDesc(faseId, null);
//        if (rodadasExistentes.hasContent()) {
//            throw new RuntimeException("Já existem rodadas criadas para a fase informado");
//        }
        // Buscar todos os atletas da fase
        List<Atleta> atletasDaFase = atletaRepository.findAllByFaseWithFilter(null, faseId, null).getContent();

        if(atletasDaFase.isEmpty()){
            throw new RegraNegocioException("A fase informada não possui competidores");
        }

        List<RodadaDto> rodadasCriadas = new ArrayList<>();

        List<Long> atletasIds = atletasDaFase.stream().map(atleta -> atleta.getId()).toList();
        int quantidadeRodadas = rodadasForms.size();

        List<List<Pair<Long, Long>>> disputas = geradorDeDisputas.geraDisputas(atletasIds, quantidadeRodadas);

        for (int i = 0; i < quantidadeRodadas; i++) {
            Rodada rodada = rodadaMapper.toEntity(rodadasForms.get(i));
            rodada.setFase(fase);
            rodada.setSituacao(SituacaoRodadaEnum.CRIADA);
            rodada.setDisputas(geraDisputaParaARodada(disputas.get(i), atletasDaFase, rodada));
            Rodada rodadaSalva = rodadaRepository.save(rodada);
            rodadasCriadas.add(rodadaMapper.toDto(rodadaSalva));
        }

        return rodadasCriadas;
    }

    private Set<Disputa> geraDisputaParaARodada(List<Pair<Long, Long>> disputasPareadas, List<Atleta> atletasDaFase, Rodada rodada) {
        List<Disputa> disputas = new ArrayList<>();

        for(Pair<Long, Long> disputaPareada : disputasPareadas){
            Atleta atletaA = selecionaAtleta(disputaPareada.getLeft(), disputaPareada.getRight(), atletasDaFase);
            Atleta atletaB = selecionaAtleta(disputaPareada.getRight(), disputaPareada.getLeft(), atletasDaFase);

            Disputa disputa = Disputa
                .builder()
                .rodada(rodada)
                .situacao(SituacaoDisputaEnum.PENDENTE)
                .build();

            RegistroDisputa registroDisputaAtletaA = RegistroDisputa.builder().atleta(atletaA).disputa(disputa).build();
            RegistroDisputa registroDisputaAtletaB = RegistroDisputa.builder().atleta(atletaB).disputa(disputa).build();

            disputa.setRegistroDisputas(new HashSet<>(Arrays.asList(registroDisputaAtletaA, registroDisputaAtletaB)));
            disputas.add(disputa);
        }

        return new HashSet<>(disputas);
    }

    private Atleta selecionaAtleta(Long idAtleta, Long idAtletaConcorrente, List<Atleta> atletas){
        if(Objects.equals(idAtleta, 0L)){
            do{
                idAtleta = atletas.get(Utils.geraValorEntreZeroAndValorMaximo(atletas.size())).getId();
            } while (idAtleta.equals(0L) || idAtleta.equals(idAtletaConcorrente));
        }

        Long finalIdAtleta = idAtleta;
        return atletas.stream().filter(atleta -> atleta.getId().equals(finalIdAtleta)).findFirst().get();

    }

    public RodadaDto finalizarRodada(UUID id) {
        Rodada rodada = rodadaRepository.findByUuid(id)
                .orElseThrow(() -> new RuntimeException("Rodada não encontrada"));

        rodada.setSituacao(SituacaoRodadaEnum.FINALIZADA);

        return rodadaMapper.toDto(rodadaRepository.save(rodada));
    }
}
