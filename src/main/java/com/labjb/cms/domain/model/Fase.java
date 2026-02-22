package com.labjb.cms.domain.model;

import com.labjb.cms.domain.enums.CriterioEntrada;
import com.labjb.cms.domain.enums.SituacaoFaseEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Builder
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_fase")
public class Fase extends BaseEntity {

    private String nome;

    @Enumerated(EnumType.STRING)
    private CriterioEntrada criterioEntrada;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    private SituacaoFaseEnum situacao;

    private Integer ordem;

    private Integer quantidadeAtletas;

    @OneToOne
    @JoinColumn(name = "fase_anterior_id")
    private Fase faseAnterior;

    @ManyToMany
    @JoinTable(
        name = "tb_fase_atleta",
        joinColumns = @JoinColumn(name = "fase_id"),
        inverseJoinColumns = @JoinColumn(name = "atleta_id")
    )
    private Set<Atleta> atletas;

    @OneToMany(mappedBy = "fase")
    private Set<Rodada> rodadas;

    @OneToMany(mappedBy = "fase")
    private Set<ResultadoFaseAtleta> resultados;

}
