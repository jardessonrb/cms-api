package com.labjb.cms.shared.mapper;

import com.labjb.cms.domain.dto.in.CampeonatoForm;
import com.labjb.cms.domain.dto.out.CampeonatoDto;
import com.labjb.cms.domain.model.Campeonato;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface CampeonatoMapper extends BaseMaps<CampeonatoDto, Campeonato, CampeonatoForm>{

    @Mappings({
            @Mapping(target = "id", source = "uuid"),
            @Mapping(target = "nomeUsuarioCriador", source = "usuarioCriador.name")
    })
    CampeonatoDto toDto(Campeonato entity);
}
