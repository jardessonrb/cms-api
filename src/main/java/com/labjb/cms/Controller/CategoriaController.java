package com.labjb.cms.Controller;

import com.labjb.cms.domain.dto.in.CategoriaForm;
import com.labjb.cms.domain.dto.out.CategoriaDto;
import com.labjb.cms.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/categoria")
@Tag(name = "Categorias", description = "Operações relacionadas a categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    @PostMapping
    @Operation(summary = "Criar nova categoria", description = "Cria uma nova categoria vinculada a um campeonato")
    public ResponseEntity<CategoriaDto> criaCategoria(@RequestBody CategoriaForm categoriaForm, UriComponentsBuilder uriComponentsBuilder) {
        CategoriaDto categoriaDto = categoriaService.criaCategoria(categoriaForm);
        URI uri = uriComponentsBuilder.path("/categoria/{id}").buildAndExpand(categoriaDto.id()).toUri();
        return ResponseEntity.created(uri).body(categoriaDto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar categoria", description = "Atualiza os dados de uma categoria existente")
    public ResponseEntity<CategoriaDto> atualizaCategoria(
            @Parameter(description = "UUID da categoria") @PathVariable UUID id,
            @RequestBody CategoriaForm categoriaForm) {
        return ResponseEntity.ok(categoriaService.atualizaCategoria(id, categoriaForm));
    }

    @GetMapping("/campeonato/{campeonatoId}")
    @Operation(summary = "Listar categorias por campeonato", description = "Lista todas as categorias de um campeonato específico")
    public ResponseEntity<List<CategoriaDto>> listarCategoriasPorCampeonato(
            @Parameter(description = "UUID do campeonato") @PathVariable UUID campeonatoId) {
        return ResponseEntity.ok(categoriaService.listarCategoriasPorCampeonato(campeonatoId));
    }
}
