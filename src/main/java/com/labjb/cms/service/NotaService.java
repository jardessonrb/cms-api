package com.labjb.cms.service;

import com.labjb.cms.domain.dto.in.NotaForm;
import com.labjb.cms.domain.dto.in.AtletaNotasForm;
import com.labjb.cms.domain.dto.in.RegistrarNotasForm;
import com.labjb.cms.domain.dto.out.DisputaDto;
import com.labjb.cms.domain.dto.out.NotaDto;
import com.labjb.cms.domain.enums.SituacaoDisputaEnum;
import com.labjb.cms.domain.model.*;
import com.labjb.cms.repository.*;
import com.labjb.cms.shared.errors.exception.RegraNegocioException;
import com.labjb.cms.shared.mapper.NotaMapper;
import com.labjb.cms.shared.mapper.DisputaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class NotaService {

    private final NotaRepository notaRepository;
    private final DisputaRepository disputaRepository;
    private final RegistroDisputaRepository registroDisputaRepository;
    private final JuradoRepository juradoRepository;
    private final NotaMapper notaMapper;
    private final DisputaMapper disputaMapper;

    @Transactional
    public DisputaDto registrarNotas(UUID disputaUuid, RegistrarNotasForm form) {
        Disputa disputa = disputaRepository.findByUuid(disputaUuid)
                .orElseThrow(() -> new RuntimeException("Disputa não encontrada"));

        // Validar atletas da disputa
        List<RegistroDisputa> registros = new ArrayList<>(disputa.getRegistroDisputas());
        if (registros.size() != 2) {
            throw new RegraNegocioException("Disputa deve ter exatamente 2 atletas");
        }

        if(form.primeiroAtleta().notas().size() != form.segundoAtleta().notas().size()){
            throw new RegraNegocioException("Quantidade de notas entre atletas não podem ser diferentes.");
        }

        List<UUID> uuidsJuradosPrimeiroAtleta = form.primeiroAtleta().notas().stream().map(nota -> nota.juradoId()).collect(Collectors.toList());
        List<UUID> uuidsJuradosSegundoAtleta = form.segundoAtleta().notas().stream().map(nota -> nota.juradoId()).collect(Collectors.toList());

        List<Jurado> jurados = juradoRepository.findByUuidIn(Stream.concat(uuidsJuradosPrimeiroAtleta.stream(), uuidsJuradosSegundoAtleta.stream()).toList());
        // Processar notas do primeiro atleta
        processarNotasAtleta(registros, form.primeiroAtleta(), jurados);

        // Processar notas do segundo atleta
        processarNotasAtleta(registros, form.segundoAtleta(), jurados);

        // Marcar disputa como concluída
        disputa.setSituacao(SituacaoDisputaEnum.CONCLUIDA);
        // Retornar disputa atualizada
        return disputaMapper.toDto(disputaRepository.save(disputa));
    }

    private void processarNotasAtleta(List<RegistroDisputa> registros, AtletaNotasForm atletaNotas, List<Jurado> jurados) {
        RegistroDisputa registroDisputa = registros.stream()
                .filter(rd -> rd.getAtleta().getUuid().equals(atletaNotas.atletaId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Atleta não encontrado na disputa"));

        for (NotaForm notaForm : atletaNotas.notas()) {
            // Validar notas (0 a 5)
            if (notaForm.notaDoAtleta() < 0 || notaForm.notaDoAtleta() > 5 ||
                notaForm.notaDaDupla() < 0 || notaForm.notaDaDupla() > 5) {
                throw new RuntimeException("Notas devem estar entre 0 e 5");
            }

            // Validar jurado
            Jurado juradoNota = jurados.stream()
                    .filter(jurado -> jurado.getUuid().equals(notaForm.juradoId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Jurado não encontrado"));

            // Verificar se jurado já deu nota para este atleta nesta disputa
            if (notaRepository.existsByJuradoUuidAndRegistroDisputaUuid(
                    notaForm.juradoId(), registroDisputa.getUuid())) {
                throw new RuntimeException("Jurado já registrou nota para este atleta nesta disputa");
            }

            // Criar nota
            Nota nota = notaMapper.toEntity(notaForm);
            nota.setJurado(juradoNota);
            nota.setRegistroDisputa(registroDisputa);

            notaRepository.save(nota);
        }
    }

    public List<NotaDto> listarNotasPorRegistroDisputa(UUID registroDisputaUuid) {
        List<Nota> notas = notaRepository.findByRegistroDisputaUuid(registroDisputaUuid);
        return notas.stream()
                .map(notaMapper::toDto)
                .toList();
    }
}
