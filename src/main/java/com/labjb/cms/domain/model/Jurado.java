package com.labjb.cms.domain.model;

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
@Table(name = "tb_jurado")
public class Jurado extends BaseEntity {

    private String nome;
    private String apelido;
    private String grupo;

    @OneToMany(mappedBy = "jurado", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Nota> notas;
}
