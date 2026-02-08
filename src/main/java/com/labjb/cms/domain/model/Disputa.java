package com.labjb.cms.domain.model;

import com.labjb.cms.domain.enums.SituacaoDisputaEnum;
import com.labjb.cms.domain.enums.TipoDisputaEnum;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Builder
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_disputa")
public class Disputa extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private SituacaoDisputaEnum situacao;

    @Enumerated(EnumType.STRING)
    private TipoDisputaEnum tipoDisputa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rodada_id")
    private Rodada rodada;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "disputa_id")
    private Set<RegistroDisputa> registroDisputas;

}
