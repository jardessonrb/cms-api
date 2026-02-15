package com.labjb.cms.service;

import com.labjb.cms.domain.dto.in.NotaForm;
import com.labjb.cms.domain.dto.in.AtletaNotasForm;
import com.labjb.cms.domain.dto.in.RegistrarNotasForm;
import com.labjb.cms.domain.dto.out.DisputaDto;
import com.labjb.cms.domain.dto.out.NotaDto;
import com.labjb.cms.domain.enums.SituacaoDisputaEnum;
import com.labjb.cms.domain.enums.SituacaoRodadaEnum;
import com.labjb.cms.domain.enums.TipoDisputaEnum;
import com.labjb.cms.domain.enums.TipoRegistroPontuacaoEnum;
import com.labjb.cms.domain.model.*;
import com.labjb.cms.repository.*;
import com.labjb.cms.shared.errors.exception.RegraNegocioException;
import com.labjb.cms.shared.mapper.NotaMapper;
import com.labjb.cms.shared.mapper.DisputaMapper;
import jakarta.persistence.EntityNotFoundException;
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


        if (disputa.getTipoDisputa() == TipoDisputaEnum.INDIVIDUAL && form.atletas().size() != 1) {
            throw new RegraNegocioException("Disputa do tipo INDIVIDUAL deve ter exatamente 1 atleta");
        }

        // Validação do tipo de disputa
        if (disputa.getTipoDisputa() == TipoDisputaEnum.DUPLA) {

            if (form.atletas().size() != 2) {
                throw new RegraNegocioException("Disputa do tipo DUPLA deve ter exatamente 2 atletas");
            }
        }

        // Validar que todos os atletas têm a mesma quantidade de notas
        if (!form.atletas().isEmpty()) {
            int quantidadeNotas = form.atletas().get(0).notas().size();
            for (AtletaNotasForm atletaNotas : form.atletas()) {
                if (atletaNotas.notas().size() != quantidadeNotas) {
                    throw new RegraNegocioException("Quantidade de notas entre atletas não podem ser diferentes.");
                }
            }
        }

        // Coletar todos os UUIDs de jurados
        Set<UUID> uuidsJurados = form.atletas().stream()
                .flatMap(atletaNotas -> atletaNotas.notas().stream())
                .map(nota -> nota.juradoId())
                .collect(Collectors.toSet());

        if(uuidsJurados.size() != 3){
            throw new RegraNegocioException("As notas devem ter exatamente 3 jurados.");
        }

        List<Jurado> jurados = juradoRepository.findByUuidIn(uuidsJurados.stream().toList());

        // Processar notas de cada atleta individualmente
        for (AtletaNotasForm atletaNotas : form.atletas()) {
            RegistroDisputa registroDisputa = disputa.getRegistroDisputas().stream()
                    .filter(rd -> rd.getAtleta().getUuid().equals(atletaNotas.atletaId()))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("Atleta não encontrado na disputa"));
            if(registroDisputa.getIsRegistradoNotas()){
                throw new RegraNegocioException("A disputa já possui notas, não pode ter mais notas registradas.");
            }

            processarNotasAtleta(registroDisputa, atletaNotas, jurados);
            
            // Marcar que as notas foram registradas para este atleta
            registroDisputa.setIsRegistradoNotas(true);
            registroDisputaRepository.save(registroDisputa);
        }

        // Marcar disputa como concluída
        disputa.setSituacao(SituacaoDisputaEnum.CONCLUIDA);
        
        // Retornar disputa atualizada
        return disputaMapper.toDto(disputaRepository.save(disputa));
    }

    private void processarNotasAtleta(RegistroDisputa registroDisputa, AtletaNotasForm atletaNotas, List<Jurado> jurados) {
        for (NotaForm notaForm : atletaNotas.notas()) {
            // Validar notas (0 a 5)
            if (notaForm.notaDoAtleta() < 0 || notaForm.notaDoAtleta() > 5 ||
                notaForm.notaDaDupla() < 0 || notaForm.notaDaDupla() > 5) {
                throw new RegraNegocioException("Notas devem estar entre 0 e 5");
            }

            // Validar jurado
            Jurado juradoNota = jurados.stream()
                    .filter(jurado -> jurado.getUuid().equals(notaForm.juradoId()))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("Jurado não encontrado"));

            // Verificar se jurado já deu nota para este atleta nesta disputa
//            if (notaRepository.existsByJuradoUuidAndRegistroDisputaUuid(
//                    notaForm.juradoId(), registroDisputa.getUuid())) {
//                throw new RegraNegocioException("Jurado já registrou nota para este atleta nesta disputa");
//            }

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

    @Transactional
    public DisputaDto atualizaNotasDisputa(UUID disputaUuid, RegistrarNotasForm form) {
        Disputa disputa = disputaRepository.findByUuid(disputaUuid)
                .orElseThrow(() -> new RegraNegocioException("Disputa não encontrada"));

        Rodada rodada = disputa.getRodada();
        if (rodada.getSituacao() == SituacaoRodadaEnum.FINALIZADA) {
            throw new RegraNegocioException("Não é possível atualizar notas de uma disputa em uma rodada finalizada");
        }

        // Coletar todos os UUIDs de jurados do formulário
        Set<UUID> uuidsJurados = form.atletas().stream()
                .flatMap(atletaNotas -> atletaNotas.notas().stream())
                .map(nota -> nota.juradoId())
                .collect(Collectors.toSet());

        if(uuidsJurados.size() != 3){
            throw new RegraNegocioException("As notas devem ter exatamente 3 jurados.");
        }
        
        Map<UUID, Map<UUID, NotaForm>> notasPorAtletaMap = new HashMap<>();
        
        for (AtletaNotasForm atletaNotas : form.atletas()) {
            Map<UUID, NotaForm> notasAtletaMap = new HashMap<>();
            
            for (NotaForm notaForm : atletaNotas.notas()) {
                if (notaForm.notaId() == null) {
                    throw new RegraNegocioException("notaId é obrigatório para atualização de notas");
                }
                notasAtletaMap.put(notaForm.notaId(), notaForm);
            }
            
            notasPorAtletaMap.put(atletaNotas.atletaId(), notasAtletaMap);
        }

        Set<RegistroDisputa> registrosDisputa = disputa.getRegistroDisputas()
                .stream()
                .filter(registroDisputa -> !registroDisputa.getTipoRegistro().equals(TipoRegistroPontuacaoEnum.NAO_PONTUADO)).collect(Collectors.toSet());

        for (RegistroDisputa registroDisputa : registrosDisputa) {
            UUID atletaId = registroDisputa.getAtleta().getUuid();
            
            if (!notasPorAtletaMap.containsKey(atletaId)) {
                throw new RegraNegocioException("Atleta com ID " + atletaId + " não encontrado no formulário de atualização");
            }
            
            Map<UUID, NotaForm> notasFormMap = notasPorAtletaMap.get(atletaId);
            
            for (Nota notaExistente : registroDisputa.getNotas()) {
                UUID notaId = notaExistente.getUuid();
                
                if (!notasFormMap.containsKey(notaId)) {
                    throw new RegraNegocioException("Nota com ID " + notaId + " do atleta " + atletaId + " não encontrada no formulário de atualização");
                }

                NotaForm notaForm = notasFormMap.get(notaId);

                Jurado juradoNota = juradoRepository.findByUuid(notaForm.juradoId())
                        .orElseThrow(() -> new RegraNegocioException("Jurado com ID " + notaForm.juradoId() + " não encontrado"));
                
                if (notaForm.notaDoAtleta() < 0 || notaForm.notaDoAtleta() > 5 ||
                    notaForm.notaDaDupla() < 0 || notaForm.notaDaDupla() > 5) {
                    throw new RegraNegocioException("Notas devem estar entre 0 e 5");
                }

                notaExistente.setNotaDoAtleta(notaForm.notaDoAtleta());
                notaExistente.setNotaDaDupla(notaForm.notaDaDupla());
                notaExistente.setJurado(juradoNota);
                
                notaRepository.save(notaExistente);
            }
        }

        return disputaMapper.toDto(disputaRepository.save(disputa));
    }
}
