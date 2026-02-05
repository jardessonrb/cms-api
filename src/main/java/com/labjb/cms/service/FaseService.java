package com.labjb.cms.service;

import com.labjb.cms.domain.dto.in.FaseForm;
import com.labjb.cms.domain.dto.out.FaseDto;
import com.labjb.cms.domain.dto.out.PontuacaoParcialDto;
import com.labjb.cms.domain.enums.CriterioEntrada;
import com.labjb.cms.domain.enums.SituacaoFaseEnum;
import com.labjb.cms.domain.model.Fase;
import com.labjb.cms.domain.model.Categoria;
import com.labjb.cms.domain.model.Atleta;
import com.labjb.cms.repository.FaseRepository;
import com.labjb.cms.repository.CategoriaRepository;
import com.labjb.cms.repository.AtletaRepository;
import com.labjb.cms.shared.mapper.FaseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class FaseService {

    private final FaseRepository faseRepository;
    private final CategoriaRepository categoriaRepository;
    private final AtletaRepository atletaRepository;
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
                (java.math.BigDecimal) row[6],  // pontuacao_por_dupla
                (java.math.BigDecimal) row[7],  // pontuacao_por_atleta
                (java.math.BigDecimal) row[8],  // total_fase
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
                
                BigDecimal totalAnterior = anterior.total();
                BigDecimal totalAtual = atual.total();
                
                if (totalAtual.compareTo(totalAnterior) == 0) {
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
}
