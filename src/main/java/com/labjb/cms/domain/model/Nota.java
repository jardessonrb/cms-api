package com.labjb.cms.domain.model;

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
@Table(name = "tb_nota")
public class Nota extends BaseEntity {

    private Integer notaDoAtleta;
    private Integer notaDaDupla;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jurado_id")
    private Jurado jurado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registro_disputa_id")
    private RegistroDisputa registroDisputa;
}
