package com.labjb.cms.service;

import com.labjb.cms.domain.dto.in.JuradoForm;
import com.labjb.cms.domain.dto.out.JuradoDto;
import com.labjb.cms.domain.model.Campeonato;
import com.labjb.cms.domain.model.Jurado;
import com.labjb.cms.repository.CampeonatoRepository;
import com.labjb.cms.repository.JuradoRepository;
import com.labjb.cms.shared.errors.exception.RegraNegocioException;
import com.labjb.cms.shared.mapper.JuradoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class JuradoService {

    private final JuradoRepository juradoRepository;
    private final CampeonatoRepository campeonatoRepository;
    private final JuradoMapper juradoMapper;

    public JuradoDto criaJurado(JuradoForm juradoForm) {
        Campeonato campeonato = campeonatoRepository.findByUuid(juradoForm.campeonatoId())
                .orElseThrow(() -> new RuntimeException("Campeonato não encontrado"));

        Integer maximoNumeroPorCampeonatoUuid = juradoRepository.findMaxNumeroByCampeonatoUuid(campeonato.getUuid());
        Jurado jurado = juradoMapper.toEntity(juradoForm);
        jurado.setCampeonato(campeonato);
        jurado.setNumero(maximoNumeroPorCampeonatoUuid + 1);

        return juradoMapper.toDto(juradoRepository.save(jurado));
    }

    public JuradoDto atualizaJurado(UUID id, JuradoForm juradoForm) {
        Jurado jurado = juradoRepository.findByUuid(id)
                .orElseThrow(() -> new RuntimeException("Jurado não encontrado"));

        jurado.setNome(juradoForm.nome());
        jurado.setApelido(juradoForm.apelido());
        jurado.setGrupo(juradoForm.grupo());

        return juradoMapper.toDto(juradoRepository.save(jurado));
    }

    public Page<JuradoDto> listarJuradosPorCampeonato(UUID campeonatoId, String filtro, Pageable pageable) {
        return juradoRepository.findAllByCampeonatoWithFilter(filtro, campeonatoId, pageable)
                .map(juradoMapper::toDto);
    }

    public JuradoDto buscarJuradoPorUuid(UUID id) {
        Jurado jurado = juradoRepository.findByUuid(id)
                .orElseThrow(() -> new RuntimeException("Jurado não encontrado"));
        return juradoMapper.toDto(jurado);
    }
}
