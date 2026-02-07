package com.labjb.cms.domain.model;

import com.labjb.cms.domain.enums.SituacaoRodadaEnum;
import com.labjb.cms.domain.enums.TipoRodadaEnum;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Builder
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_rodada")
public class Rodada extends BaseEntity {

    private String nome;

    @Enumerated(EnumType.STRING)
    private SituacaoRodadaEnum situacao;

    @Enumerated(EnumType.STRING)
    private TipoRodadaEnum tipoRodada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fase_id")
    private Fase fase;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "rodada_id")
    private Set<Disputa> disputas;

    private Integer atletasParaProximaFase;
}
