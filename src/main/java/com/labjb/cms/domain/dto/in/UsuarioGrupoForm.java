package com.labjb.cms.domain.dto.in;

import java.util.UUID;

public record UsuarioGrupoForm(String email, String senha, String nome, UUID grupoId) {
}
