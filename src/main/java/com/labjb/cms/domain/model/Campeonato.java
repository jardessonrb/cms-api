package com.labjb.cms.domain.model;

import com.labjb.cms.domain.enums.SituacaoCampeonatoEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Builder
@Entity
@Data
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
}
