package com.labjb.cms.shared.mapper;

import com.labjb.cms.domain.dto.in.CategoriaForm;
import com.labjb.cms.domain.dto.out.CategoriaDto;
import com.labjb.cms.domain.model.Categoria;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface CategoriaMapper extends BaseMaps<CategoriaDto, Categoria, CategoriaForm> {

    @Mappings({
            @Mapping(target = "id", source = "uuid")
    })
    CategoriaDto toDto(Categoria entity);
}
