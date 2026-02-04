package com.labjb.cms.shared.mapper;

import com.labjb.cms.domain.dto.in.JuradoForm;
import com.labjb.cms.domain.dto.out.JuradoDto;
import com.labjb.cms.domain.model.Jurado;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface JuradoMapper extends BaseMaps<JuradoDto, Jurado, JuradoForm> {

    @Mappings({
            @Mapping(target = "id", source = "uuid")
    })
    JuradoDto toDto(Jurado entity);
}
