package com.labjb.cms.service;

import com.labjb.cms.domain.dto.in.FaseForm;
import com.labjb.cms.domain.dto.out.FaseDto;
import com.labjb.cms.domain.dto.out.PontuacaoParcialDto;
import com.labjb.cms.domain.dto.out.ValidacaoCorteDto;
import com.labjb.cms.domain.dto.out.ResultadoFaseAtletaDto;
import com.labjb.cms.domain.enums.CriterioEntrada;
import com.labjb.cms.domain.enums.SituacaoFaseEnum;
import com.labjb.cms.domain.model.Fase;
import com.labjb.cms.domain.model.Categoria;
import com.labjb.cms.domain.model.Atleta;
import com.labjb.cms.domain.model.ResultadoFaseAtleta;
import com.labjb.cms.repository.FaseRepository;
import com.labjb.cms.repository.CategoriaRepository;
import com.labjb.cms.repository.AtletaRepository;
import com.labjb.cms.repository.ResultadoFaseAtletaRepository;
import com.labjb.cms.repository.RodadaRepository;
import com.labjb.cms.shared.errors.exception.RegraNegocioException;
import com.labjb.cms.shared.mapper.FaseMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

    public FaseDto criaFase(FaseForm faseForm) {
        Categoria categoria = categoriaRepository.findByUuid(faseForm.categoriaId())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        Fase fase = faseMapper.toEntity(faseForm);
        fase.setCategoria(categoria);
        fase.setSituacao(SituacaoFaseEnum.CRIADA);
        fase.setOrdem(Math.toIntExact(faseRepository.findMaxOrdemByCategoriaUuid(categoria.getUuid()) + 1));

        // Se critério for TODOS, adicionar todos os atletas da categoria
        if (faseForm.criterioEntrada() == CriterioEntrada.TODOS) {
            List<Atleta> atletasCategoria = atletaRepository.findAllByCategoriaWithFilter(null, faseForm.categoriaId(), null)
                    .getContent();
            fase.setAtletas(atletasCategoria.stream().collect(Collectors.toSet()));
        } else if (faseForm.criterioEntrada() == CriterioEntrada.N_PRIMEIROS) {
            // Para N_PRIMEIROS, validar que existe fase anterior e está finalizada
            if (faseForm.faseAnterior() == null) {
                throw new RegraNegocioException("Critério N_PRIMEIROS exige uma fase anterior");
            }
            
            Fase faseAnterior = faseRepository.findByUuid(faseForm.faseAnterior())
                    .orElseThrow(() -> new EntityNotFoundException("Fase anterior não encontrada"));
            
            if (faseAnterior.getSituacao() != SituacaoFaseEnum.FINALIZADA) {
                throw new RegraNegocioException("Fase anterior deve estar FINALIZADA para usar critério N_PRIMEIROS");
            }
            
            // TODO: Implementar lógica para buscar os N primeiros atletas da fase anterior
            // Por enquanto, não adiciona atletas (será feito em outro método)
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

    public List<PontuacaoParcialDto> buscarPontuacaoParcial(UUID faseId) {
        Fase fase = faseRepository.findByUuid(faseId)
                .orElseThrow(() -> new RuntimeException("Fase não encontrada"));
        
        List<Object[]> results = faseRepository.findPontuacaoParcialByFaseId(fase.getId());
        List<PontuacaoParcialDto> pontuacoes = new ArrayList<>();
        
        // Primeiro, criar todos os DTOs sem posição
        for (Object[] row : results) {
            pontuacoes.add(new PontuacaoParcialDto(
                ((Number) row[0]).longValue(),  // atletaId
                (String) row[1],  // categoria
                (String) row[2],  // fase
                (String) row[3],  // competidor
                ((Number) row[4]).longValue(),  // partidas
                ((Number) row[5]).longValue(),  // partidas_concluidas
                ((Number) row[6]).doubleValue(),  // pontuacao_por_dupla
                ((Number) row[7]).doubleValue(),  // pontuacao_por_atleta
                ((Number) row[8]).doubleValue(),  // total_fase
                null  // posicao será calculada abaixo
            ));
        }
        
        // Agora calcular posições e criar novos objetos com posição
        List<PontuacaoParcialDto> resultadoFinal = new ArrayList<>();
        if (!pontuacoes.isEmpty()) {
            int posicaoAtual = 1;
            
            // Primeiro elemento sempre recebe posição 1
            PontuacaoParcialDto primeiro = pontuacoes.get(0);
            resultadoFinal.add(new PontuacaoParcialDto(
                primeiro.atletaId(),
                primeiro.categoria(), primeiro.fase(), primeiro.competidor(),
                primeiro.partidas(), primeiro.partidasConcluidas(), 
                primeiro.notaIndividual(), primeiro.notaDupla(), primeiro.total(),
                posicaoAtual
            ));
            
            // Demais elementos
            for (int i = 1; i < pontuacoes.size(); i++) {
                PontuacaoParcialDto atual = pontuacoes.get(i);
                PontuacaoParcialDto anterior = pontuacoes.get(i - 1);
                
                Double totalAnterior = anterior.total();
                Double totalAtual = atual.total();
                
                if (totalAtual.equals(totalAnterior)) {
                    // Mesmo total, mesma posição
                    resultadoFinal.add(new PontuacaoParcialDto(
                        atual.atletaId(),
                        atual.categoria(), atual.fase(), atual.competidor(),
                        atual.partidas(), atual.partidasConcluidas(), 
                        atual.notaIndividual(), atual.notaDupla(), atual.total(),
                        posicaoAtual
                    ));
                } else {
                    // Total diferente, nova posição
                    posicaoAtual = i + 1;
                    resultadoFinal.add(new PontuacaoParcialDto(
                        atual.atletaId(),
                        atual.categoria(), atual.fase(), atual.competidor(),
                        atual.partidas(), atual.partidasConcluidas(), 
                        atual.notaIndividual(), atual.notaDupla(), atual.total(),
                        posicaoAtual
                    ));
                }
            }
        }
        
        return resultadoFinal;
    }

    public FaseDto finalizarFase(UUID faseId) {
        // Validar existência da fase
        Fase fase = faseRepository.findByUuid(faseId)
                .orElseThrow(() -> new EntityNotFoundException("Fase não encontrada"));

        // Verificar se a fase está INICIADA
        if (fase.getSituacao() != SituacaoFaseEnum.INICIADA) {
            throw new RegraNegocioException("Fase deve estar com situação INICIADA para ser finalizada");
        }

        // Verificar se todas as rodadas estão FINALIZADAS
        Long rodadasNaoFinalizadas = rodadaRepository.countRodadasNaoFinalizadasByFaseUuid(faseId);
        if (rodadasNaoFinalizadas > 0) {
            throw new RegraNegocioException("Todas as rodadas devem estar FINALIZADAS para finalizar a fase");
        }

        // Buscar pontuação parcial
        List<PontuacaoParcialDto> pontuacoes = buscarPontuacaoParcial(faseId);

        // Criar mapa com chave sendo o ID do atleta
        Map<Long, PontuacaoParcialDto> pontuacaoMap = pontuacoes.stream()
                .collect(Collectors.toMap(PontuacaoParcialDto::atletaId, dto -> dto));

        // Limpar resultados existentes (se houver)
        resultadoFaseAtletaRepository.deleteByFaseUuid(faseId);

        // Salvar resultados para cada atleta da fase
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

        // Atualizar situação da fase para FINALIZADA
        fase.setSituacao(SituacaoFaseEnum.FINALIZADA);
        Fase faseSalva = faseRepository.save(fase);

        return faseMapper.toDto(faseSalva);
    }

    public ValidacaoCorteDto validaCorte(UUID faseAnteriorUuid, Integer quantidadeAtletas) {
        if(quantidadeAtletas < 1){
            throw new RegraNegocioException("Quantidade de atletas deve ser positiva.");
        }
        // Validar existência da fase anterior
        Fase faseAnterior = faseRepository.findByUuid(faseAnteriorUuid)
                .orElseThrow(() -> new EntityNotFoundException("Fase anterior não encontrada"));

        // Verificar se a fase anterior está FINALIZADA
        if (faseAnterior.getSituacao() != SituacaoFaseEnum.FINALIZADA) {
            throw new RegraNegocioException("Fase anterior deve estar FINALIZADA para validar o corte");
        }

        Pair<List<ResultadoFaseAtleta>, List<ResultadoFaseAtleta>> atletas = separaAtletasClassificadosEEmpatadosPorQuantidade(faseAnteriorUuid, quantidadeAtletas);

        List<ResultadoFaseAtletaDto> atletasEmpatados = atletas
                .getRight()
                .stream()
                .map(r -> new ResultadoFaseAtletaDto(r.getId(), r.getAtleta().getUuid(),
                        r.getAtleta().getApelido() + " - " + r.getAtleta().getGrupo(), r.getNotaIndividual(),
                        r.getNotaDupla(), r.getTotal(), r.getPosicao()))
                .toList();

        return new ValidacaoCorteDto(atletasEmpatados.size(), atletasEmpatados);
    }

    private Pair<List<ResultadoFaseAtleta>, List<ResultadoFaseAtleta>> separaAtletasClassificadosEEmpatadosPorQuantidade(UUID faseAnteriorUuid, Integer quantidadeAtletas) {
        // Buscar resultados da fase anterior ordenados por posição
        List<ResultadoFaseAtleta> resultados = resultadoFaseAtletaRepository
                .findByFaseUuidOrderByTotalDesc(faseAnteriorUuid);

        if(quantidadeAtletas > resultados.size()){
            throw new RegraNegocioException("A quantidade informada é maior que a quantidade de atletas na fase");
        }

        // Buscar os N primeiros atletas por pontuação (ordenados por posição)
        Double totalDoUltimoAtletaDaQuantidade = resultados.stream()
                .limit(quantidadeAtletas)
                .toList()
                .getLast()
                .getTotal();

        List<ResultadoFaseAtleta> atletasNaProximaFaseDireto = new ArrayList<>();
        List<ResultadoFaseAtleta> candidatosAProximaFase = new ArrayList<>();
        for(ResultadoFaseAtleta resultado : resultados){
            if(resultado.getTotal() > totalDoUltimoAtletaDaQuantidade){
                atletasNaProximaFaseDireto.add(resultado);
            }else if(resultado.getTotal().equals(totalDoUltimoAtletaDaQuantidade)){
                candidatosAProximaFase.add(resultado);
            }
        }

        if((candidatosAProximaFase.size() + atletasNaProximaFaseDireto.size()) == quantidadeAtletas){
            atletasNaProximaFaseDireto.addAll(candidatosAProximaFase);
            return Pair.of(atletasNaProximaFaseDireto, new ArrayList<>());
        }

        return Pair.of(atletasNaProximaFaseDireto, candidatosAProximaFase);
    }
}
