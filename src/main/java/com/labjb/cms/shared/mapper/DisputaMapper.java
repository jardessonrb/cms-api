package com.labjb.cms.shared.mapper;

import com.labjb.cms.domain.dto.in.DisputaForm;
import com.labjb.cms.domain.dto.out.DisputaDto;
import com.labjb.cms.domain.dto.out.RegistroDisputaDto;
import com.labjb.cms.domain.model.Disputa;
import com.labjb.cms.domain.model.RegistroDisputa;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
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
                        registro.getTipoRegistro()
                ))
                .toList();
    }
}
