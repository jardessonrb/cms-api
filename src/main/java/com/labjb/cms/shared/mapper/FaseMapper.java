package com.labjb.cms.shared.mapper;

import com.labjb.cms.domain.dto.in.FaseForm;
import com.labjb.cms.domain.dto.out.FaseDto;
import com.labjb.cms.domain.model.Fase;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface FaseMapper extends BaseMaps<FaseDto, Fase, FaseForm> {

    @Mappings({
            @Mapping(target = "id", source = "uuid"),
            @Mapping(target = "quantidadeRodadas", expression = "java(entity.getRodadas() != null ? entity.getRodadas().size() : 0)"),
            @Mapping(target = "isCompartilhada", source = "isCompartilhada")
    })
    FaseDto toDto(Fase entity);
}
