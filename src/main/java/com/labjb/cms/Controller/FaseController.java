package com.labjb.cms.Controller;

import com.labjb.cms.domain.dto.in.FaseForm;
import com.labjb.cms.domain.dto.out.FaseDto;
import com.labjb.cms.service.FaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/fase")
@Tag(name = "Fases", description = "Operações relacionadas a fases das competições")
public class FaseController {

    private final FaseService faseService;

    @PostMapping
    @Operation(summary = "Criar nova fase", description = "Cria uma nova fase vinculada a uma categoria")
    public ResponseEntity<FaseDto> criaFase(@RequestBody FaseForm faseForm, UriComponentsBuilder uriComponentsBuilder) {
        FaseDto faseDto = faseService.criaFase(faseForm);
        URI uri = uriComponentsBuilder.path("/fase/{id}").buildAndExpand(faseDto.id()).toUri();
        return ResponseEntity.created(uri).body(faseDto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar fase", description = "Atualiza os dados de uma fase existente")
    public ResponseEntity<FaseDto> atualizaFase(
            @Parameter(description = "UUID da fase") @PathVariable UUID id,
            @RequestBody FaseForm faseForm) {
        return ResponseEntity.ok(faseService.atualizaFase(id, faseForm));
    }

    @GetMapping("/categoria/{categoriaId}")
    @Operation(summary = "Listar fases por categoria", description = "Lista todas as fases de uma categoria específica paginadas")
    public ResponseEntity<Page<FaseDto>> listarFasesPorCategoria(
            @Parameter(description = "UUID da categoria") @PathVariable UUID categoriaId,
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        return ResponseEntity.ok(faseService.listarFasesPorCategoria(categoriaId, pageable));
    }
}
