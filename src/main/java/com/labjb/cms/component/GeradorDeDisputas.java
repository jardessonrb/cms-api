package com.labjb.cms.component;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class GeradorDeDisputas {

    public List<List<Pair<Long, Long>>> geraDisputas(List<Long> atletas, int quantidadeRodadas) {
        List<List<Pair<Long, Long>>> chaves = new ArrayList<>();
        List<Long> lista = new ArrayList<>(atletas);

        // Se for ímpar, adiciona BYE = 0L
        if (lista.size() % 2 != 0) {
            lista.add(0L);
        }

        int n = lista.size();
//        int maxRodadasPossiveis = n - 1;
//        int rodadas = Math.min(quantidadeRodadas, quantidadeRodadas);

        for (int r = 0; r < quantidadeRodadas; r++) {
            List<Pair<Long, Long>> disputas = new ArrayList<>();

            for (int i = 0; i < n / 2; i++) {
                Long a = lista.get(i);
                Long b = lista.get(n - 1 - i);

                // Se quiser ignorar BYE, não adiciona disputa com 0
//                if (a != 0L && b != 0L) {
//                    disputas.add(Pair.of(a, b));
//                } else {
                // Se quiser registrar folga, você pode guardar:
                disputas.add(Pair.of(a, b));
//                 }
            }

            // Rotação: mantém o primeiro fixo
            Long fixo = lista.get(0);
            List<Long> resto = new ArrayList<>(lista.subList(1, n));
            Collections.rotate(resto, 1);

            lista.clear();
            lista.add(fixo);
            lista.addAll(resto);
            chaves.add(disputas);
        }

        return chaves;
    }

    public List<Long> embaralha(List<Long> atletas){
        List<Long> atletasEmbaralhados = new ArrayList<>(atletas);

        Collections.shuffle(atletasEmbaralhados);

        return atletasEmbaralhados;
    }

    // Exemplo de uso
//    public static void main(String[] args) {
////        List<Long> atletas = List.of(
////                1L,  2L,  3L,  4L,  5L,  6L,  7L,  8L,  9L, 10L,
////                11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L,
////                21L, 22L, 23L, 24L, 25L, 26L, 27L, 28L, 29L, 30L
////        );
//
//        List<Long> atletas = List.of(1L,  2L,  3L,  4L, 5L, 6L, 7L);
//        int rodadas = 3;
//
//        List<List<Pair<Long, Long>>> resultado = gerarDisputas(atletas, rodadas);
//
//        int numero = 1;
//        for (List<Pair<Long, Long>> chaves : resultado) {
//            System.out.println("Rodada "+numero+"°");
//            for(Pair<Long, Long> chave : chaves){
//                System.out.println(chave.getLeft() + " vs " + chave.getRight());
//            }
//            numero++;
//        }
//    }
}

