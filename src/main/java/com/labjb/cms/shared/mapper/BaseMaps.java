package com.labjb.cms.shared.mapper;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface BaseMaps<DTO, ENTITY, FORM> {
    DTO toDto(ENTITY entity);
    ENTITY toEntity(FORM form);
    FORM toForm(ENTITY entity);
    List<ENTITY> toEntity(List<FORM> forms);
    List<DTO> toDto(List<ENTITY> entities);
}
