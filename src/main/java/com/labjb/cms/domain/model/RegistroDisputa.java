package com.labjb.cms.domain.model;

import com.labjb.cms.domain.enums.TipoRegistroPontuacaoEnum;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Builder
@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_registro_disputa")
public class RegistroDisputa extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atleta_id")
    private Atleta atleta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disputa_id")
    private Disputa disputa;

    @Enumerated(EnumType.STRING)
    private TipoRegistroPontuacaoEnum tipoRegistro;

    @Builder.Default
    private Boolean isRegistradoNotas = false;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "registro_disputa_id")
    private Set<Nota> notas;
}
