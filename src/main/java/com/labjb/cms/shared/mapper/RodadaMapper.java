package com.labjb.cms.shared.mapper;

import com.labjb.cms.domain.dto.in.RodadaForm;
import com.labjb.cms.domain.dto.out.RodadaDto;
import com.labjb.cms.domain.model.Rodada;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface RodadaMapper extends BaseMaps<RodadaDto, Rodada, RodadaForm> {

    @Mappings({
            @Mapping(target = "id", source = "uuid")
    })
    RodadaDto toDto(Rodada entity);
}
