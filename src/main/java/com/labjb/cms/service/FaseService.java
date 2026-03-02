package com.labjb.cms.service;

import com.labjb.cms.component.SeparadorAtletasComponent;
import com.labjb.cms.domain.dto.in.FaseForm;
import com.labjb.cms.domain.dto.out.FaseDto;
import com.labjb.cms.domain.dto.out.PontuacaoParcialDto;
import com.labjb.cms.domain.dto.out.ResultadoFaseAtletaDto;
import com.labjb.cms.domain.dto.out.ValidacaoCorteDto;
import com.labjb.cms.domain.enums.*;
import com.labjb.cms.domain.model.*;
import com.labjb.cms.repository.*;
import com.labjb.cms.shared.errors.exception.RegraNegocioException;
import com.labjb.cms.shared.mapper.FaseMapper;
import com.labjb.cms.shared.utils.Utils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class FaseService {

    private final FaseRepository faseRepository;
    private final CategoriaRepository categoriaRepository;
    private final AtletaRepository atletaRepository;
    private final ResultadoFaseAtletaRepository resultadoFaseAtletaRepository;
    private final RodadaRepository rodadaRepository;
    private final FaseMapper faseMapper;
    private final SeparadorAtletasComponent separadorAtletasComponent;
    private final CompartilhamentoRepository compartilhamentoRepository;
    private final CampeonatoRepository campeonatoRepository;
    private final double VALOR_MAXIMO_DESEMPATE = Integer.MAX_VALUE;

    @Transactional
    public FaseDto criaFase(FaseForm faseForm) {
        Categoria categoria = categoriaRepository.findByUuid(faseForm.categoriaId())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        Fase fase = faseMapper.toEntity(faseForm);
        fase.setCategoria(categoria);
        fase.setSituacao(SituacaoFaseEnum.CRIADA);
        fase.setOrdem(Math.toIntExact(faseRepository.findMaxOrdemByCategoriaUuid(categoria.getUuid()) + 1));
        fase.setIsCompartilhada(false);

        // Se critério for TODOS, adicionar todos os atletas da categoria
        if (faseForm.criterioEntrada() == CriterioEntrada.TODOS) {
            List<Atleta> atletasCategoria = atletaRepository.findAllByCategoriaWithFilter(null, faseForm.categoriaId(), SituacaoAtletaEnum.ATIVO, null)
                    .getContent();
            fase.setAtletas(atletasCategoria.stream().collect(Collectors.toSet()));
        } else if (faseForm.criterioEntrada() == CriterioEntrada.N_PRIMEIROS) {
            // Para N_PRIMEIROS, validar que existe fase anterior e está finalizada
            if (faseForm.faseAnteriorId() == null) {
                throw new RegraNegocioException("Critério N_PRIMEIROS exige uma fase anterior");
            }

            Fase faseAnterior = faseRepository.findByUuid(faseForm.faseAnteriorId())
                    .orElseThrow(() -> new EntityNotFoundException("Fase anterior não encontrada"));

            if (faseAnterior.getSituacao() != SituacaoFaseEnum.FINALIZADA) {
                throw new RegraNegocioException("Fase anterior deve estar FINALIZADA para usar critério N_PRIMEIROS");
            }

            if(faseAnterior.getRodadas().stream().anyMatch(rodada -> rodada.getTipoRodada().equals(TipoRodadaEnum.DESEMPATE))){
                throw new RegraNegocioException(String.format("Fase %s já possui uma rodada de desempate.", faseAnterior.getNome()));
            }

            List<ResultadoFaseAtleta> resultadosDaFaseAnterior = resultadoFaseAtletaRepository.findByFaseUuidOrderByTotalDesc(faseForm.faseAnteriorId())
                    .stream()
                    .filter(resultado -> resultado.getAtleta().getSituacao() == SituacaoAtletaEnum.ATIVO)
                    .collect(Collectors.toList());

            // Buscar atletas classificados e empatados
            var atletasSeparados = separadorAtletasComponent.separaAtletasClassificadosEEmpatadosPorQuantidade(resultadosDaFaseAnterior, faseForm.quantidadeAtletas());

            List<ResultadoFaseAtleta> atletasClassificadosDireto = atletasSeparados.getLeft();
            List<ResultadoFaseAtleta> atletasEmpatados = atletasSeparados.getRight();

            // Se houver atletas empatados, criar rodada de desempate na fase anterior
            if (!atletasEmpatados.isEmpty()) {
                Integer atletasParaProximaFase = faseForm.quantidadeAtletas() - atletasClassificadosDireto.size();

                Rodada rodadaDesempate = Rodada.builder()
                        .nome("Rodada de Desempate - " + faseAnterior.getNome())
                        .situacao(SituacaoRodadaEnum.CRIADA)
                        .tipoRodada(TipoRodadaEnum.DESEMPATE)
                        .fase(faseAnterior)
                        .atletasParaProximaFase(atletasParaProximaFase)
                        .disputas(new HashSet<>())
                        .build();

                // Criar disputas para cada atleta empatado
                for (ResultadoFaseAtleta atletaEmpatado : atletasEmpatados) {
                    Disputa disputa = Disputa.builder()
                            .situacao(SituacaoDisputaEnum.PENDENTE)
                            .rodada(rodadaDesempate)
                            .tipoDisputa(TipoDisputaEnum.INDIVIDUAL)
                            .registroDisputas(new HashSet<>())
                            .build();

                    // Criar registro de disputa para o atleta
                    RegistroDisputa registroDisputa = RegistroDisputa.builder()
                            .atleta(atletaEmpatado.getAtleta())
                            .disputa(disputa)
                            .tipoRegistro(TipoRegistroPontuacaoEnum.PONTUADO)
                            .notas(new HashSet<>())
                            .build();

                    disputa.getRegistroDisputas().add(registroDisputa);
                    rodadaDesempate.getDisputas().add(disputa);
                }

                // Salvar a rodada de desempate na fase anterior
                rodadaRepository.save(rodadaDesempate);

                fase.setSituacao(SituacaoFaseEnum.AGUARDANDO_DESEMPATE);

                faseAnterior.setSituacao(SituacaoFaseEnum.INICIADA);
                faseRepository.save(faseAnterior);
            }

            fase.setAtletas(atletasClassificadosDireto.stream()
                    .map(ResultadoFaseAtleta::getAtleta)
                    .collect(Collectors.toSet()));
            fase.setFaseAnterior(faseAnterior);
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

    public FaseDto buscarFasePorUuid(UUID uuid) {
        Fase fase = faseRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Fase não encontrada"));
        return faseMapper.toDto(fase);
    }

    public Page<FaseDto> listarFasesPorCategoria(UUID categoriaId, String filtro, Pageable pageable) {
        return faseRepository.findByCategoriaUuidOrderByOrdemDesc(filtro, categoriaId, pageable)
                .map(faseMapper::toDto);
    }

    public List<PontuacaoParcialDto> buscarPontuacaoParcial(UUID faseId) {
        Fase fase = faseRepository.findByUuid(faseId)
                .orElseThrow(() -> new RuntimeException("Fase não encontrada"));

        List<PontuacaoParcialDto> pontuacoes = extraiPontuacaoFase(fase);

        // Agora calcular posições e criar novos objetos com posição
        List<PontuacaoParcialDto> resultadoFinal = new ArrayList<>();
        if (!pontuacoes.isEmpty()) {
            int posicaoAtual = 1;

            // Primeiro elemento sempre recebe posição 1
            PontuacaoParcialDto primeiro = pontuacoes.get(0);
            resultadoFinal.add(new PontuacaoParcialDto(
                primeiro.atletaId(),
                primeiro.situacao(),
                primeiro.categoria(), primeiro.fase(), primeiro.competidor(), primeiro.numeroCompetidor(),
                primeiro.partidas(), primeiro.partidasConcluidas(), 
                primeiro.notaIndividual(), primeiro.notaDupla(), primeiro.total(),
                primeiro.totalDesempate() >= VALOR_MAXIMO_DESEMPATE ? -1 : primeiro.totalDesempate(),
                posicaoAtual
            ));
            
            // Demais elementos
            for (int i = 1; i < pontuacoes.size(); i++) {
                PontuacaoParcialDto atual = pontuacoes.get(i);
                PontuacaoParcialDto anterior = pontuacoes.get(i - 1);
                
                Double totalAnterior = anterior.total();
                Double totalAtual = atual.total();

                double totalDesempate = atual.totalDesempate() >= VALOR_MAXIMO_DESEMPATE ? -1 : atual.totalDesempate();
                if (totalAtual.equals(totalAnterior)) {
                    // Mesmo total, mesma posição
                    resultadoFinal.add(new PontuacaoParcialDto(
                        atual.atletaId(),
                        atual.situacao(),
                        atual.categoria(), atual.fase(), atual.competidor(), atual.numeroCompetidor(),
                        atual.partidas(), atual.partidasConcluidas(), 
                        atual.notaIndividual(), atual.notaDupla(), atual.total(),
                        totalDesempate,
                        posicaoAtual
                    ));
                } else {
                    // Total diferente, nova posição
                    posicaoAtual++;
                    resultadoFinal.add(new PontuacaoParcialDto(
                        atual.atletaId(),
                        atual.situacao(),
                        atual.categoria(), atual.fase(), atual.competidor(), atual.numeroCompetidor(),
                        atual.partidas(), atual.partidasConcluidas(), 
                        atual.notaIndividual(), atual.notaDupla(), atual.total(),
                        totalDesempate,
                        posicaoAtual
                    ));
                }
            }
        }
        
        return resultadoFinal;
    }

    private List<PontuacaoParcialDto> extraiPontuacaoFase(Fase fase) {
        List<Object[]> resultados = faseRepository.findPontuacaoParcialByFaseId(fase.getId());

        List<PontuacaoParcialDto> pontuacoes = new ArrayList<>();
        // Primeiro, criar todos os DTOs sem posição
        for (Object[] row : resultados) {
            pontuacoes.add(new PontuacaoParcialDto(
                ((Number) row[0]).longValue(),  // atletaId
                (String) row[1],  // situacao
                (String) row[2],  // categoria
                (String) row[3],  // fase
                (String) row[4],  // competidor
                row[5] != null ? ((Number) row[5]).intValue() : null,  // numeroCompetidor
                ((Number) row[6]).longValue(),  // partidas
                ((Number) row[7]).longValue(),  // partidas_concluidas
                Utils.arredondar(((Number) row[8]).doubleValue()),  // pontuacao_por_dupla
                Utils.arredondar(((Number) row[9]).doubleValue()),  // pontuacao_por_atletas
                Utils.arredondar(((Number) row[10]).doubleValue()),  // total_fase
                Utils.arredondar(((Number) row[11]).doubleValue()),  // total_fase_desempate
                null  // posicao será calculada abaixo
            ));
        }

        return pontuacoes;
    }

    public FaseDto finalizarFase(UUID faseId) {
        // Validar existência da fase
        Fase fase = faseRepository.findByUuid(faseId)
                .orElseThrow(() -> new EntityNotFoundException("Fase não encontrada"));

        // Verificar se a fase está INICIADA
        if (fase.getSituacao() == SituacaoFaseEnum.FINALIZADA) {
            throw new RegraNegocioException("A fase já encontra-se finalizada.");
        }

        // Verificar se todas as rodadas estão FINALIZADAS
        Long rodadasNaoFinalizadas = rodadaRepository.countRodadasNaoFinalizadasByFaseUuid(faseId);
        if (rodadasNaoFinalizadas > 0) {
            throw new RegraNegocioException("Todas as rodadas devem estar FINALIZADAS para finalizar a fase");
        }

        // Salvar resultados para cada atleta da fase
        if(fase.getResultados().isEmpty()){
            List<PontuacaoParcialDto> pontuacoes = buscarPontuacaoParcial(faseId);

            Map<Long, PontuacaoParcialDto> pontuacaoMap = pontuacoes.stream()
                    .collect(Collectors.toMap(PontuacaoParcialDto::atletaId, dto -> dto));

            for (Atleta atleta : fase.getAtletas()) {
                PontuacaoParcialDto pontuacao = pontuacaoMap.get(atleta.getId());

                if (pontuacao != null) {
                    ResultadoFaseAtleta resultado = ResultadoFaseAtleta.builder()
                            .fase(fase)
                            .atleta(atleta)
                            .notaIndividual(pontuacao.notaIndividual())
                            .notaDupla(pontuacao.notaDupla())
                            .total(pontuacao.total())
                            .posicao(pontuacao.posicao())
                            .build();

                    resultadoFaseAtletaRepository.save(resultado);
                }
            }
        }

        // Atualizar situação da fase para FINALIZADA
        fase.setSituacao(SituacaoFaseEnum.FINALIZADA);
        Fase faseSalva = faseRepository.save(fase);

        return faseMapper.toDto(faseSalva);
    }

    public ValidacaoCorteDto validaCorte(UUID faseAnteriorUuid, Integer quantidadeAtletas) {
        // Validar existência da fase anterior
        Fase faseAnterior = faseRepository.findByUuid(faseAnteriorUuid)
                .orElseThrow(() -> new EntityNotFoundException("Fase anterior não encontrada"));

        // Verificar se a fase anterior está FINALIZADA
        if (faseAnterior.getSituacao() != SituacaoFaseEnum.FINALIZADA) {
            throw new RegraNegocioException("Fase anterior deve estar FINALIZADA para validar o corte");
        }

        // Buscar resultados da fase anterior ordenados por posição
        List<ResultadoFaseAtleta> resultados = resultadoFaseAtletaRepository
                .findByFaseUuidOrderByTotalDesc(faseAnteriorUuid)
                .stream()
                .filter(resultado -> resultado.getAtleta().getSituacao() == SituacaoAtletaEnum.ATIVO)
                .sorted((a, b) -> Double.compare(b.getTotal(), a.getTotal()))
                .toList();

        Pair<List<ResultadoFaseAtleta>, List<ResultadoFaseAtleta>> atletas = separadorAtletasComponent.separaAtletasClassificadosEEmpatadosPorQuantidade(resultados, quantidadeAtletas);

        List<ResultadoFaseAtletaDto> atletasEmpatados = atletas
                .getRight()
                .stream()
                .map(r -> new ResultadoFaseAtletaDto(r.getId(), r.getAtleta().getUuid(),
                        r.getAtleta().getApelido() + " - " + r.getAtleta().getGrupo(), r.getAtleta().getNumero(), r.getNotaIndividual(),
                        r.getNotaDupla(), r.getTotal(), r.getPosicao()))
                .toList();

        return new ValidacaoCorteDto(atletasEmpatados.size(), atletasEmpatados);
    }

    @Transactional
    public FaseDto compartilharFase(UUID faseId) {
        // Validar existência da fase
        Fase fase = faseRepository.findByUuid(faseId)
                .orElseThrow(() -> new EntityNotFoundException("Fase não encontrada"));

        // Obter o campeonato da categoria
        Long campeonatoId = fase.getCategoria().getCampeonato().getId();

        // Verificar se o campeonato tem compartilhamento habilitado
        Compartilhamento compartilhamento = compartilhamentoRepository.findByCampeonatoId(campeonatoId)
                .orElseThrow(() -> new RuntimeException("Campeonato não possui compartilhamento habilitado"));

        if (!compartilhamento.getIsHabilitado()) {
            throw new RuntimeException("Compartilhamento do campeonato não está habilitado");
        }

        // Descompartilhar todas as outras fases do campeonato
        faseRepository.desabilitarCompartilhamentoPorCampeonatoId(campeonatoId);

        // Compartilhar a fase atual
        fase.setIsCompartilhada(true);
        Fase faseSalva = faseRepository.save(fase);

        return faseMapper.toDto(faseSalva);
    }

    @Transactional
    public FaseDto pararCompartilhamentoFase(UUID faseId) {
        // Validar existência da fase
        Fase fase = faseRepository.findByUuid(faseId)
                .orElseThrow(() -> new EntityNotFoundException("Fase não encontrada"));

        // Obter o campeonato da categoria
        Long campeonatoId = fase.getCategoria().getCampeonato().getId();

        // Verificar se o campeonato tem compartilhamento habilitado
        Compartilhamento compartilhamento = compartilhamentoRepository.findByCampeonatoId(campeonatoId)
                .orElseThrow(() -> new RuntimeException("Campeonato não possui compartilhamento habilitado"));

        if (!compartilhamento.getIsHabilitado()) {
            throw new RuntimeException("Compartilhamento do campeonato não está habilitado");
        }

        // Verificar se esta fase está compartilhada
        if (!fase.getIsCompartilhada()) {
            throw new RuntimeException("Fase não está compartilhada");
        }

        // Descompartilhar a fase
        fase.setIsCompartilhada(false);
        Fase faseSalva = faseRepository.save(fase);

        return faseMapper.toDto(faseSalva);
    }

    @Transactional
    public void adicionarAtletaFase(UUID faseId, UUID atletaId) {
        // Validar existência da fase
        Fase fase = faseRepository.findByUuid(faseId)
                .orElseThrow(() -> new EntityNotFoundException("Fase não encontrada"));

        // Validar se a fase não está finalizada
        if (fase.getSituacao() == SituacaoFaseEnum.FINALIZADA) {
            throw new RegraNegocioException("Não é possível adicionar atletas a uma fase finalizada");
        }

        // Validar existência do atleta
        Atleta atleta = atletaRepository.findByUuid(atletaId)
                .orElseThrow(() -> new EntityNotFoundException("Atleta não encontrado"));

        // Validar se o atleta está ativo
        if (atleta.getSituacao() != SituacaoAtletaEnum.ATIVO) {
            throw new RegraNegocioException("Apenas atletas ativos podem ser adicionados à fase");
        }

        // Validar se o atleta pertence à mesma categoria da fase
        if (!atletaRepository.existsAtletaInCategoria(atletaId, fase.getCategoria().getUuid())) {
            throw new RegraNegocioException("O atleta não pertence à categoria desta fase");
        }

        // Validar se o atleta já está na fase
        if (fase.getAtletas().contains(atleta)) {
            throw new RegraNegocioException("O atleta já está nesta fase");
        }

        // Adicionar o atleta à fase
        fase.getAtletas().add(atleta);
        
        // Verificar se a fase já tem rodadas geradas
        if (!fase.getRodadas().isEmpty()) {
            // Para cada rodada da fase, criar disputa individual para o novo atleta
            for (Rodada rodada : fase.getRodadas()) {
                // Se a rodada estiver finalizada, voltar para criada
                if (rodada.getSituacao() == SituacaoRodadaEnum.FINALIZADA) {
                    rodada.setSituacao(SituacaoRodadaEnum.CRIADA);
                }
                
                // Buscar um outro atleta aleatório da fase (exceto o novo atleta)
                List<Atleta> outrosAtletas = fase.getAtletas().stream()
                        .filter(a -> !a.equals(atleta) && !a.getSituacao().equals(SituacaoAtletaEnum.CANCELADO))
                        .collect(Collectors.toList());
                
                if (!outrosAtletas.isEmpty()) {
                    // Selecionar atleta aleatório
                    Random random = new Random();
                    Atleta atletaOponente = outrosAtletas.get(random.nextInt(outrosAtletas.size()));
                    
                    // Criar disputa individual
                    Disputa disputa = Disputa.builder()
                            .rodada(rodada)
                            .situacao(SituacaoDisputaEnum.PENDENTE)
                            .tipoDisputa(TipoDisputaEnum.INDIVIDUAL)
                            .build();
                    
                    // Criar registros de disputa para os dois atletas
                    RegistroDisputa registroNovoAtleta = RegistroDisputa.builder()
                            .atleta(atleta)
                            .disputa(disputa)
                            .tipoRegistro(TipoRegistroPontuacaoEnum.PONTUADO)
                            .build();
                    
                    RegistroDisputa registroOponente = RegistroDisputa.builder()
                            .atleta(atletaOponente)
                            .disputa(disputa)
                            .tipoRegistro(TipoRegistroPontuacaoEnum.NAO_PONTUADO)
                            .build();
                    
                    disputa.setRegistroDisputas(new HashSet<>(Arrays.asList(registroNovoAtleta, registroOponente)));
                    
                    // Adicionar disputa à rodada
                    rodada.getDisputas().add(disputa);
                }
            }
        }
        
        faseRepository.save(fase);
    }
}
