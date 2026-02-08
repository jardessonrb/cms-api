package com.labjb.cms.component;

import com.labjb.cms.domain.model.ResultadoFaseAtleta;
import com.labjb.cms.shared.errors.exception.RegraNegocioException;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SeparadorAtletasComponent {

    public Pair<List<ResultadoFaseAtleta>, List<ResultadoFaseAtleta>> separaAtletasClassificadosEEmpatadosPorQuantidade(
            List<ResultadoFaseAtleta> resultados, Integer quantidadeAtletas) {
        
        if(quantidadeAtletas < 1 || quantidadeAtletas > resultados.size()){
            throw new RegraNegocioException("A quantidade de atletas não pode ser menor que 1 ou maior que a quantidade de atletas na fase");
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
