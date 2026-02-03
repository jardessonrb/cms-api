package com.labjb.cms.shared.mapper;

import com.labjb.cms.domain.dto.in.AtletaForm;
import com.labjb.cms.domain.dto.out.AtletaDto;
import com.labjb.cms.domain.model.Atleta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface AtletaMapper extends BaseMaps<AtletaDto, Atleta, AtletaForm> {

    @Mappings({
            @Mapping(target = "id", source = "uuid")
    })
    AtletaDto toDto(Atleta entity);
}
