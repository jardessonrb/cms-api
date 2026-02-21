package com.labjb.cms.service;

import com.labjb.cms.domain.dto.in.GrupoForm;
import com.labjb.cms.domain.dto.out.GrupoDto;
import com.labjb.cms.domain.model.Grupo;
import com.labjb.cms.domain.model.User;
import com.labjb.cms.repository.GrupoRepository;
import com.labjb.cms.repository.UserRepository;
import com.labjb.cms.shared.errors.exception.EntityAlreadyException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class GrupoService {

    private final GrupoRepository grupoRepository;
    private final UserRepository userRepository;

    public GrupoDto criarGrupo(GrupoForm grupoForm) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User proprietario = (User) authentication.getPrincipal();
        
        if (grupoRepository.existsByNome(grupoForm.nome())) {
            throw new RuntimeException("Já existe um grupo com este nome");
        }

        proprietario = userRepository.findById(proprietario.getId())
                .orElseThrow(() -> new EntityAlreadyException("Usário logado não encontrado."));

        Grupo grupo = new Grupo();
        grupo.setNome(grupoForm.nome());
        grupo.setProprietario(proprietario);
        grupo.getUsuarios().add(proprietario);

        // Associar o grupo ao usuário proprietário (relacionamento de membro)
        proprietario.setGrupo(grupo);
        
        // Associar o grupo como proprietário do usuário (relacionamento de propriedade)
        proprietario.setGrupoProprietario(grupo);
        
        Grupo savedGrupo = grupoRepository.save(grupo);

        return new GrupoDto(
                savedGrupo.getUuid(),
                savedGrupo.getNome(),
                savedGrupo.getProprietario().getName(),
                savedGrupo.getCriadoEm()
        );
    }

    public GrupoDto buscarGrupoPorId(UUID id) {
        Grupo grupo = grupoRepository.findByUuid(id)
                .orElseThrow(() -> new EntityNotFoundException("Grupo não encontrado"));

        return new GrupoDto(
                grupo.getUuid(),
                grupo.getNome(),
                grupo.getProprietario().getName(),
                grupo.getCriadoEm()
        );
    }
}
