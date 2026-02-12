package com.labjb.cms.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Builder
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_atleta")
public class Atleta extends BaseEntity {

    private String nome;
    private Integer numero;
    private String apelido;
    private String responsavel;
    private LocalDate dataNascimento;
    private String cidade;
    private String grupo;
    private String graduacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campeonato_id")
    private Campeonato campeonato;

    @OneToMany(mappedBy = "atleta")
    private Set<InscricaoCategoria> inscricoesCategoria;

    @ManyToMany(mappedBy = "atletas")
    private Set<Fase> fases;

    @ManyToMany
    @JoinTable(
            name = "tb_registro_disputa_atleta",
            joinColumns = @JoinColumn(name = "atleta_id"),
            inverseJoinColumns = @JoinColumn(name = "registro_disputa_id")
    )
    private Set<RegistroDisputa> registrosDisputa;

    @OneToMany(mappedBy = "atleta")
    private Set<ResultadoFaseAtleta> resultadosFase;
}
