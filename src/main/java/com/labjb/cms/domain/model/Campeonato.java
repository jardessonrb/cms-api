package com.labjb.cms.domain.model;

import com.labjb.cms.domain.enums.SituacaoCampeonatoEnum;
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
@Table(name = "tb_campeonato")
public class Campeonato extends BaseEntity {

    private String nome;

    @Enumerated(EnumType.STRING)
    private SituacaoCampeonatoEnum situacao;

    @OneToMany(mappedBy = "campeonato", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Atleta> atletas;

    @OneToMany(mappedBy = "campeonato", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Categoria> categorias;

    @OneToMany(mappedBy = "campeonato", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Jurado> jurados;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_id")
    private Grupo grupo;

    @OneToOne(mappedBy = "campeonato", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Compartilhamento compartilhamento;
}
