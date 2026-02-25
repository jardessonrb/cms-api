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
@Table(name = "tb_compartilhamento")
public class Compartilhamento extends BaseEntity {

    @Column(nullable = false)
    private Boolean isHabilitado = false;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "campeonato_id")
    private Campeonato campeonato;
}
