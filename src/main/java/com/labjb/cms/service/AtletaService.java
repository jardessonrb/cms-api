package com.labjb.cms.service;

import com.labjb.cms.domain.dto.in.AtletaForm;
import com.labjb.cms.domain.dto.out.AtletaDto;
import com.labjb.cms.domain.model.Atleta;
import com.labjb.cms.domain.model.Campeonato;
import com.labjb.cms.repository.AtletaRepository;
import com.labjb.cms.repository.CampeonatoRepository;
import com.labjb.cms.shared.mapper.AtletaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AtletaService {

    private final AtletaRepository atletaRepository;
    private final CampeonatoRepository campeonatoRepository;
    private final AtletaMapper atletaMapper;

    public AtletaDto criaAtleta(AtletaForm atletaForm) {
        Campeonato campeonato = campeonatoRepository.findByUuid(atletaForm.campeonatoId())
                .orElseThrow(() -> new RuntimeException("Campeonato não encontrado"));

        Atleta atleta = atletaMapper.toEntity(atletaForm);
        atleta.setCampeonato(campeonato);

        return atletaMapper.toDto(atletaRepository.save(atleta));
    }

    public AtletaDto atualizaAtleta(UUID id, AtletaForm atletaForm) {
        Atleta atleta = atletaRepository.findByUuid(id)
                .orElseThrow(() -> new RuntimeException("Atleta não encontrado"));

        atleta.setNome(atletaForm.nome());
        atleta.setApelido(atletaForm.apelido());
        atleta.setResponsavel(atletaForm.responsavel());
        atleta.setDataNascimento(atletaForm.dataNascimento());
        atleta.setCidade(atletaForm.cidade());
        atleta.setGrupo(atletaForm.grupo());
        atleta.setGraduacao(atletaForm.graduacao());

        return atletaMapper.toDto(atletaRepository.save(atleta));
    }

    public Page<AtletaDto> listarAtletas(String filtro, UUID campeonatoId, Pageable pageable) {
        return atletaRepository.findAllWithFilter(filtro, campeonatoId, pageable)
                .map(atletaMapper::toDto);
    }

    public AtletaDto visualizarAtleta(UUID id) {
        return atletaRepository.findByUuid(id)
                .map(atletaMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Atleta não encontrado"));
    }
}
