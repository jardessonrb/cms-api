package com.labjb.cms.shared.mapper;

import com.labjb.cms.domain.dto.in.NotaForm;
import com.labjb.cms.domain.dto.out.NotaDto;
import com.labjb.cms.domain.model.Nota;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface NotaMapper extends BaseMaps<NotaDto, Nota, NotaForm> {

    @Mappings({
            @Mapping(target = "id", source = "uuid"),
            @Mapping(target = "juradoId", source = "jurado.uuid"),
            @Mapping(target = "juradoNome", source = "jurado.nome")
    })
    NotaDto toDto(Nota entity);
}
