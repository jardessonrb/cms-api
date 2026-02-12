package com.labjb.cms.service;

import com.labjb.cms.domain.dto.in.AtletaForm;
import com.labjb.cms.domain.dto.out.AtletaDto;
import com.labjb.cms.domain.enums.SituacaoInscricaoCategoriaEnum;
import com.labjb.cms.domain.model.Atleta;
import com.labjb.cms.domain.model.Campeonato;
import com.labjb.cms.domain.model.Categoria;
import com.labjb.cms.domain.model.InscricaoCategoria;
import com.labjb.cms.repository.AtletaRepository;
import com.labjb.cms.repository.CampeonatoRepository;
import com.labjb.cms.repository.CategoriaRepository;
import com.labjb.cms.repository.InscricaoCategoriaRepository;
import com.labjb.cms.shared.errors.exception.RegraNegocioException;
import com.labjb.cms.shared.mapper.AtletaMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AtletaService {

    private final AtletaRepository atletaRepository;
    private final CategoriaRepository categoriaRepository;
    private final InscricaoCategoriaRepository inscricaoCategoriaRepository;
    private final CampeonatoRepository campeonatoRepository;
    private final AtletaMapper atletaMapper;

    public AtletaDto criaAtleta(AtletaForm atletaForm) {
        Campeonato campeonato = campeonatoRepository.findByUuid(atletaForm.campeonatoId())
                .orElseThrow(() -> new EntityNotFoundException("Campeonato não encontrado"));

        // Validar se o número já existe no campeonato
        if (atletaForm.numero() != null) {
            atletaRepository.findByNumeroAndCampeonatoUuid(atletaForm.numero(), atletaForm.campeonatoId())
                    .ifPresent(atleta -> {
                        throw new RegraNegocioException("Já existe um atleta com o número " + atletaForm.numero() + " neste campeonato");
                    });
        }

        Atleta atleta = atletaMapper.toEntity(atletaForm);
        atleta.setCampeonato(campeonato);
        atleta = atletaRepository.save(atleta);

        if(Objects.nonNull(atletaForm.categoriaId())){
            inscreveAtletaNaCategoria(atletaForm, atleta);
        }

        return atletaMapper.toDto(atleta);
    }

    private void inscreveAtletaNaCategoria(AtletaForm atletaForm, Atleta atleta) {
        Categoria categoria = categoriaRepository
                .findByUuidAndCampeonatoUuid(atletaForm.categoriaId(), atletaForm.campeonatoId())
                .orElse(null);

        if(Objects.nonNull(categoria)){
            InscricaoCategoria inscricao = InscricaoCategoria.builder()
                    .atleta(atleta)
                    .categoria(categoria)
                    .situacao(SituacaoInscricaoCategoriaEnum.ATIVO)
                    .build();
            inscricaoCategoriaRepository.save(inscricao);
        }
    }

    public AtletaDto atualizaAtleta(UUID id, AtletaForm atletaForm) {
        Atleta atleta = atletaRepository.findByUuid(id)
                .orElseThrow(() -> new RuntimeException("Atleta não encontrado"));

        // Validar se o número já existe no campeonato (apenas se for diferente do atual)
        if (atletaForm.numero() != null && !atletaForm.numero().equals(atleta.getNumero())) {
            atletaRepository.findByNumeroAndCampeonatoUuid(atletaForm.numero(), atleta.getCampeonato().getUuid())
                    .ifPresent(atletaExistente -> {
                        throw new RegraNegocioException("Já existe um atleta com o número " + atletaForm.numero() + " neste campeonato");
                    });
        }

        atleta.setNome(atletaForm.nome());
        atleta.setNumero(atletaForm.numero());
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

    public Page<AtletaDto> listarAtletasPorCategoria(String filtro, UUID categoriaId, Pageable pageable) {
        return atletaRepository.findAllByCategoriaWithFilter(filtro, categoriaId, pageable)
                .map(atletaMapper::toDto);
    }

    public Page<AtletaDto> listarAtletasPorFase(String filtro, UUID faseId, Pageable pageable) {
        return atletaRepository.findAllByFaseWithFilter(filtro, faseId, pageable)
                .map(atletaMapper::toDto);
    }

    public AtletaDto visualizarAtleta(UUID id) {
        return atletaRepository.findByUuid(id)
                .map(atletaMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Atleta não encontrado"));
    }
}
