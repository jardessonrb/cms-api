package com.labjb.cms.domain.model;

import com.labjb.cms.domain.enums.SituacaoCategoriaEnum;
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
@Table(name = "tb_categoria")
public class Categoria extends BaseEntity {

    private String nome;

    @Enumerated(EnumType.STRING)
    private SituacaoCategoriaEnum situacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campeonato_id")
    private Campeonato campeonato;

    @OneToMany(mappedBy = "categoria")
    private Set<InscricaoCategoria> inscricoesAtleta;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "categoria_id")
    private Set<Fase> fases;
}
