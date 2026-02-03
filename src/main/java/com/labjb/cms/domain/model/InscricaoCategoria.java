package com.labjb.cms.domain.model;

import com.labjb.cms.domain.enums.SituacaoInscricaoCategoriaEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_inscricao_categoria")
public class InscricaoCategoria extends BaseEntity {

    @ManyToOne
    private Atleta atleta;

    @ManyToOne
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    private SituacaoInscricaoCategoriaEnum situacao;
}
