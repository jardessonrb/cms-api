package com.labjb.cms.domain.dto.out;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AtletaImportacaoDto(
    LocalDateTime carimboDataHora,
    String nomeCompleto,
    String apelido,
    String nomeApelidoProfessorMestre,
    LocalDate dataNascimento,
    String graduacaoCorda,
    String cidade,
    String tamanhoCamisa,
    String sexo,
    String categoria,
    String grupoEscola
){}
