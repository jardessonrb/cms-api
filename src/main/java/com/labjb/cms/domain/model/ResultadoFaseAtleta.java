package com.labjb.cms.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_resultado_fase_atleta")
public class ResultadoFaseAtleta extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "fase_id")
    private Fase fase;

    @ManyToOne
    @JoinColumn(name = "atleta_id")
    private Atleta atleta;

    private Double notaIndividual;

    private Double notaDupla;

    private Double total;

    private Integer posicao;
}
