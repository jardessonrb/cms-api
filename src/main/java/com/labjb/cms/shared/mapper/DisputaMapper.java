package com.labjb.cms.shared.mapper;

import com.labjb.cms.domain.dto.in.DisputaForm;
import com.labjb.cms.domain.dto.out.DisputaDto;
import com.labjb.cms.domain.dto.out.NotaDto;
import com.labjb.cms.domain.dto.out.RegistroDisputaDto;
import com.labjb.cms.domain.model.Disputa;
import com.labjb.cms.domain.model.Nota;
import com.labjb.cms.domain.model.RegistroDisputa;
import org.aspectj.weaver.ast.Not;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {NotaMapper.class})
public interface DisputaMapper extends BaseMaps<DisputaDto, Disputa, DisputaForm> {

    @Mappings({
            @Mapping(target = "id", source = "uuid"),
            @Mapping(target = "registrosDisputa", expression = "java(mapearRegistrosDisputa(entity.getRegistroDisputas()))")
    })
    DisputaDto toDto(Disputa entity);

    default List<RegistroDisputaDto> mapearRegistrosDisputa(Set<RegistroDisputa> registros) {
        return registros.stream()
                .map(registro -> new RegistroDisputaDto(
                        registro.getUuid(),
                        registro.getAtleta().getNome(),
                        registro.getAtleta().getNumero(),
                        registro.getAtleta().getApelido(),
                        registro.getAtleta().getUuid(),
                        registro.getTipoRegistro(),
                        mapearNotasToDto(registro.getNotas())
                ))
                .toList();
    }

    default List<NotaDto> mapearNotasToDto(Set<Nota> notas){
        return Objects.isNull(notas) ? new ArrayList<>() : notas.stream()
                .map(nota -> NotaDto
                                .builder()
                                .id(nota.getUuid())
                                .notaDoAtleta(nota.getNotaDoAtleta())
                                .notaDaDupla(nota.getNotaDaDupla())
                                .juradoId(nota.getJurado().getUuid())
                                .juradoNome((nota.getJurado().getApelido() != null ? nota.getJurado().getApelido() + " - " : " ") + nota.getJurado().getNome())
                                .build())
                .collect(Collectors.toList());
    }
}
