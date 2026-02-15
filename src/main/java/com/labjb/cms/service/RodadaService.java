package com.labjb.cms.service;

import com.labjb.cms.component.GeradorDeDisputas;
import com.labjb.cms.component.SeparadorAtletasComponent;
import com.labjb.cms.domain.dto.in.RodadaForm;
import com.labjb.cms.domain.dto.out.RodadaDto;
import com.labjb.cms.domain.dto.out.FaseDto;
import com.labjb.cms.domain.enums.*;
import com.labjb.cms.domain.model.*;
import com.labjb.cms.repository.AtletaRepository;
import com.labjb.cms.repository.DisputaRepository;
import com.labjb.cms.repository.FaseRepository;
import com.labjb.cms.repository.RodadaRepository;
import com.labjb.cms.shared.errors.exception.RegraNegocioException;
import com.labjb.cms.shared.mapper.RodadaMapper;
import com.labjb.cms.shared.mapper.FaseMapper;
import com.labjb.cms.shared.utils.Utils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class RodadaService {

    private final RodadaRepository rodadaRepository;
    private final FaseRepository faseRepository;
    private final AtletaRepository atletaRepository;
    private final DisputaRepository disputaRepository;
    private final RodadaMapper rodadaMapper;
    private final FaseMapper faseMapper;
    private final GeradorDeDisputas geradorDeDisputas;
    private final SeparadorAtletasComponent separadorAtletasComponent;

    public RodadaDto criaRodada(RodadaForm rodadaForm) {
        Fase fase = faseRepository.findByUuid(rodadaForm.faseId())
                .orElseThrow(() -> new RuntimeException("Fase não encontrada"));

        Rodada rodada = rodadaMapper.toEntity(rodadaForm);
        rodada.setFase(fase);
        rodada.setSituacao(SituacaoRodadaEnum.INICIADA);

        Rodada rodadaSalva = rodadaRepository.save(rodada);
        return rodadaMapper.toDto(rodadaSalva);
    }

    public RodadaDto atualizaRodada(UUID id, RodadaForm rodadaForm) {
        Rodada rodada = rodadaRepository.findByUuid(id)
                .orElseThrow(() -> new RuntimeException("Rodada não encontrada"));

        rodada.setNome(rodadaForm.nome());
        Rodada rodadaSalva = rodadaRepository.save(rodada);
        return rodadaMapper.toDto(rodadaSalva);
    }

    public Page<RodadaDto> listarRodadasPorFase(UUID faseId, String filtro, Pageable pageable) {
        Page<Rodada> rodadasPage = rodadaRepository.findByFaseUuidWithFilter(faseId, filtro, pageable);
        
        return rodadasPage.map(rodada -> adicionaContagemDeDisputaDaRodada(rodada));
    }

    private @NonNull RodadaDto adicionaContagemDeDisputaDaRodada(Rodada rodada) {
        // Buscar contagem de disputas para cada rodada
        List<Object[]> disputasCount = rodadaRepository.findDisputasCountByRodadaId(rodada.getId());

        Long disputasConcluidas = 0L;
        Long disputasPendentes = 0L;

        if (!disputasCount.isEmpty()) {
            Object[] result = disputasCount.get(0);
            disputasConcluidas = ((Number) result[1]).longValue();
            disputasPendentes = ((Number) result[2]).longValue();
        }

        // Criar DTO com as contagens
        RodadaDto dto = rodadaMapper.toDto(rodada);
        return new RodadaDto(
                dto.id(),
                dto.nome(),
                dto.situacao(),
                dto.tipoRodada(),
                dto.atletasParaProximaFase(),
                dto.criadoEm(),
                disputasConcluidas,
                disputasPendentes
        );
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

            RegistroDisputa registroDisputaAtletaA = RegistroDisputa.builder().atleta(atletaA).disputa(disputa).tipoRegistro(disputaPareada.getLeft().equals(0L) ? TipoRegistroPontuacaoEnum.NAO_PONTUADO : TipoRegistroPontuacaoEnum.PONTUADO).build();
            RegistroDisputa registroDisputaAtletaB = RegistroDisputa.builder().atleta(atletaB).disputa(disputa).tipoRegistro(disputaPareada.getRight().equals(0L) ? TipoRegistroPontuacaoEnum.NAO_PONTUADO : TipoRegistroPontuacaoEnum.PONTUADO).build();

            disputa.setRegistroDisputas(new HashSet<>(Arrays.asList(registroDisputaAtletaA, registroDisputaAtletaB)));
            disputa.setTipoDisputa(DisputaService.defineTipoDisputaComBaseEmRegistrosDisputa(disputa.getRegistroDisputas().stream().collect(Collectors.toList())));
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

        // Verificar se todas as disputas associadas estão CONCLUIDA
        Long disputasNaoConcluidas = disputaRepository.countDisputasNaoConcluidasByRodadaUuid(id);
        if (disputasNaoConcluidas > 0) {
            throw new RegraNegocioException("Todas as disputas devem estar CONCLUIDA para finalizar a rodada");
        }

        // Se for rodada de desempate, executar lógica específica
        if (rodada.getTipoRodada() == TipoRodadaEnum.DESEMPATE) {
            finalizarRodadaDesempate(rodada);
            Rodada rodadaSalva = rodadaRepository.save(rodada);
            return rodadaMapper.toDto(rodadaSalva);
        }

        rodada.setSituacao(SituacaoRodadaEnum.FINALIZADA);
        Rodada rodadaSalva = rodadaRepository.save(rodada);
        return rodadaMapper.toDto(rodadaSalva);
    }

    @Transactional
    public FaseDto finalizarRodadaDesempate(Rodada rodada) {
        // 1° Verificar se é uma rodada de desempate
        if (rodada.getTipoRodada() != TipoRodadaEnum.DESEMPATE) {
            throw new RegraNegocioException("Rodada deve ser do tipo DESEMPATE");
        }

        // 2° Buscar todas as disputas da rodada e verificar se estão CONCLUIDAS
        for (Disputa disputa : rodada.getDisputas()) {
            if (disputa.getSituacao() != SituacaoDisputaEnum.CONCLUIDA) {
                throw new RegraNegocioException("Todas as disputas da rodada de desempate devem estar CONCLUIDAS");
            }
        }

        // 3° Calcular a média das notas individuais de cada atleta na rodada
        List<ResultadoFaseAtleta> resultadosDesempate = new ArrayList<>();
        
        for (Disputa disputa : rodada.getDisputas()) {
            for (RegistroDisputa registro : disputa.getRegistroDisputas()) {
                Atleta atleta = registro.getAtleta();
                
                // Calcular média das notas individuais do atleta nesta rodada
                Double mediaNotasIndividuais = registro.getNotas().stream()
                        .mapToDouble(nota -> nota.getNotaDoAtleta())
                        .average()
                        .orElse(0.0);
                
                ResultadoFaseAtleta resultado = ResultadoFaseAtleta.builder()
                        .atleta(atleta)
                        .notaIndividual(mediaNotasIndividuais)
                        .notaDupla(0.0) // Não relevante para desempate
                        .total(mediaNotasIndividuais)
                        .build();
                
                resultadosDesempate.add(resultado);
            }
        }

        // Ordenar resultados pelo total (maior para menor)
        resultadosDesempate.sort((a, b) -> Double.compare(b.getTotal(), a.getTotal()));

        // 4° Verificar se há empates na quantidade de atletas que devem passar
        Integer atletasParaProximaFase = rodada.getAtletasParaProximaFase();
        var atletasSeparados = separadorAtletasComponent.separaAtletasClassificadosEEmpatadosPorQuantidade(
                resultadosDesempate, atletasParaProximaFase);
        
        List<ResultadoFaseAtleta> atletasClassificados = atletasSeparados.getLeft();
        List<ResultadoFaseAtleta> atletasEmpatados = atletasSeparados.getRight();

        if (!atletasEmpatados.isEmpty()) {
            throw new RegraNegocioException("Não pode haver empates na rodada de desempate. Existem atletas empatados na posição de corte.");
        }

        // 5° Pegar os primeiros atletas e adicionar na fase que está aguardando desempate
        Set<Atleta> atletasParaAdicionar = atletasClassificados.stream()
                .map(ResultadoFaseAtleta::getAtleta)
                .collect(Collectors.toSet());

        // Buscar fase que está aguardando desempate (próxima fase na mesma categoria)
        Fase faseAnterior = rodada.getFase();
        List<Fase> fasesCategoria = faseRepository.findByCategoriaUuidOrderByOrdemDesc(faseAnterior.getCategoria().getUuid(), null)
                .getContent()
                .stream()
                .filter(f -> f.getOrdem() > faseAnterior.getOrdem())
                .sorted((a, b) -> Integer.compare(a.getOrdem(), b.getOrdem()))
                .toList();

        if (fasesCategoria.isEmpty()) {
            throw new RegraNegocioException("Não foi encontrada uma fase aguardando desempate");
        }

        Fase faseAguardandoDesempate = fasesCategoria.get(0);
        if (faseAguardandoDesempate.getSituacao() != SituacaoFaseEnum.AGUARDANDO_DESEMPATE) {
            throw new RegraNegocioException("A próxima fase não está aguardando desempate");
        }

        // Adicionar atletas classificados à fase
        faseAguardandoDesempate.getAtletas().addAll(atletasParaAdicionar);

        // 6° Mudar a situação da fase AGUARDANDO_DESEMPATE para CRIADA
        faseAguardandoDesempate.setSituacao(SituacaoFaseEnum.CRIADA);

        // 7° Encerrar a rodada de desempate como FINALIZADA
        rodada.setSituacao(SituacaoRodadaEnum.FINALIZADA);

        // Salvar as alterações
        rodadaRepository.save(rodada);

        return faseMapper.toDto(faseRepository.save(faseAguardandoDesempate));
    }
}
