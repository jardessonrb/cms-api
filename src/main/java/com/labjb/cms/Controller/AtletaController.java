package com.labjb.cms.Controller;

import com.labjb.cms.domain.dto.in.AtletaForm;
import com.labjb.cms.domain.dto.out.AtletaDto;
import com.labjb.cms.service.AtletaService;
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
@RequestMapping("/atleta")
@Tag(name = "Atletas", description = "Operações relacionadas a atletas")
public class AtletaController {

    private final AtletaService atletaService;

    @PostMapping
    @Operation(summary = "Criar novo atleta", description = "Cria um novo atleta vinculado a um campeonato")
    public ResponseEntity<AtletaDto> criaAtleta(@RequestBody AtletaForm atletaForm, UriComponentsBuilder uriComponentsBuilder) {
        AtletaDto atletaDto = atletaService.criaAtleta(atletaForm);
        URI uri = uriComponentsBuilder.path("/atleta/{id}").buildAndExpand(atletaDto.id()).toUri();
        return ResponseEntity.created(uri).body(atletaDto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar atleta", description = "Atualiza todos os dados simples de um atleta existente")
    public ResponseEntity<AtletaDto> atualizaAtleta(
            @Parameter(description = "UUID do atleta") @PathVariable UUID id,
            @RequestBody AtletaForm atletaForm) {
        return ResponseEntity.ok(atletaService.atualizaAtleta(id, atletaForm));
    }

    @GetMapping("/campeonato/{campeonatoId}")
    @Operation(summary = "Listar atletas", description = "Lista atletas paginados de um campeonato específico, ordenados por apelido e nome, com filtro opcional")
    public ResponseEntity<Page<AtletaDto>> listarAtletas(
            @Parameter(description = "Filtro para busca por nome, apelido ou graduação") @RequestParam(required = false) String filtro,
            @Parameter(description = "UUID do campeonato") @PathVariable UUID campeonatoId,
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        return ResponseEntity.ok(atletaService.listarAtletas(filtro, campeonatoId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Visualizar atleta", description = "Visualiza os dados de um atleta específico")
    public ResponseEntity<AtletaDto> visualizarAtleta(
            @Parameter(description = "UUID do atleta") @PathVariable UUID id) {
        return ResponseEntity.ok(atletaService.visualizarAtleta(id));
    }
}
