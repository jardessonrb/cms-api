package com.labjb.cms.shared.mapper;

import com.labjb.cms.domain.dto.in.AtletaForm;
import com.labjb.cms.domain.dto.out.AtletaDto;
import com.labjb.cms.domain.dto.out.NotaDto;
import com.labjb.cms.domain.model.Atleta;
import com.labjb.cms.domain.model.InscricaoCategoria;
import com.labjb.cms.domain.model.Nota;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.*;

@Mapper(componentModel = "spring")
public interface AtletaMapper extends BaseMaps<AtletaDto, Atleta, AtletaForm> {

    @Mappings({
            @Mapping(target = "id", source = "uuid"),
            @Mapping(target = "categoriaId", expression = "java(getCategoriaId(entity))"),
            @Mapping(target = "categoria", expression = "java(getNomeCategoria(entity))")
    })
    AtletaDto toDto(Atleta entity);


    default UUID getCategoriaId(Atleta entity){
        if(Objects.nonNull(entity.getInscricoesCategoria())){
            Optional<InscricaoCategoria> inscricaoCategoria = entity.getInscricoesCategoria().stream().findFirst();
            if(inscricaoCategoria.isPresent()){
                return inscricaoCategoria.get().getCategoria().getUuid();
            }
        }

        return null;
    }

    default String getNomeCategoria(Atleta entity){
        if(Objects.nonNull(entity.getInscricoesCategoria())){
            Optional<InscricaoCategoria> inscricaoCategoria = entity.getInscricoesCategoria().stream().findFirst();
            if(inscricaoCategoria.isPresent()){
                return inscricaoCategoria.get().getCategoria().getNome();
            }
        }

        return null;
    }
}
