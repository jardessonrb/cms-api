package com.labjb.cms.Controller;

import com.labjb.cms.domain.dto.in.CampeonatoForm;
import com.labjb.cms.domain.dto.out.CampeonatoDto;
import com.labjb.cms.domain.dto.out.CampeonatoDetalhadoDto;
import com.labjb.cms.domain.dto.out.CompartilhamentoDto;
import com.labjb.cms.service.CampeonatoService;
import com.labjb.cms.service.CompartilhamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/campeonato")
@Tag(name = "Campeonatos", description = "Operações relacionadas a campeonatos")
public class CampeonatoController {

    private final CampeonatoService campeonatoService;
    private final CompartilhamentoService compartilhamentoService;

    @PostMapping
    @Operation(summary = "Criar novo campeonato", description = "Cria um novo campeonato no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Campeonato criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos")
    })
    public ResponseEntity<CampeonatoDto> criaCampeonato(@RequestBody CampeonatoForm campeonatoForm, UriComponentsBuilder uriComponentsBuilder){
        CampeonatoDto campeonatoDto = campeonatoService.criaCampeonato(campeonatoForm);
        URI uri = uriComponentsBuilder.path("/campeonato/{id}").buildAndExpand(campeonatoDto.id()).toUri();
        return ResponseEntity.created(uri).body(campeonatoDto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar campeonato", description = "Atualiza os dados de um campeonato existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Campeonato atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Campeonato não encontrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos")
    })
    public ResponseEntity<CampeonatoDto> atualizaCampeonato(
            @Parameter(description = "UUID do campeonato") @PathVariable UUID id,
            @RequestBody CampeonatoForm campeonatoForm) {
        return ResponseEntity.ok(campeonatoService.atualizaCampeonato(id, campeonatoForm));
    }

    @GetMapping
    @Operation(summary = "Listar campeonatos", description = "Lista todos os campeonatos paginados e ordenados por data de criação descendente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de campeonatos retornada com sucesso")
    })
    public ResponseEntity<Page<CampeonatoDto>> listarCampeonatos(@PageableDefault(size = 10, page = 0) Pageable pageable) {
        return ResponseEntity.ok(campeonatoService.listarCampeonatos(pageable));
    }

    @GetMapping("/{uuid}")
    @Operation(summary = "Buscar campeonato por UUID", description = "Busca um campeonato específico pelo UUID com estatísticas detalhadas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Campeonato encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Campeonato não encontrado")
    })
    public ResponseEntity<CampeonatoDetalhadoDto> buscarCampeonatoPorUuid(
            @Parameter(description = "UUID do campeonato") @PathVariable UUID uuid) {
        return ResponseEntity.ok(campeonatoService.buscarCampeonatoPorUuid(uuid));
    }

    @PostMapping("/{campeonatoId}/criar-compartilhamento")
    @Operation(summary = "Criar compartilhamento", description = "Cria um compartilhamento para o campeonato")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Compartilhamento criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou compartilhamento já existe"),
            @ApiResponse(responseCode = "404", description = "Campeonato não encontrado")
    })
    public ResponseEntity<CompartilhamentoDto> criarCompartilhamento(
            @Parameter(description = "UUID do campeonato") @PathVariable UUID campeonatoId) {
        return ResponseEntity.status(201).body(compartilhamentoService.criarCompartilhamento(campeonatoId));
    }

    @PutMapping("/{campeonatoId}/habilitar-compartilhamento")
    @Operation(summary = "Habilitar compartilhamento", description = "Habilita o compartilhamento do campeonato")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compartilhamento habilitado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Campeonato ou compartilhamento não encontrado")
    })
    public ResponseEntity<CompartilhamentoDto> habilitarCompartilhamento(
            @Parameter(description = "UUID do campeonato") @PathVariable UUID campeonatoId) {
        return ResponseEntity.ok(compartilhamentoService.habilitarCompartilhamento(campeonatoId));
    }

    @PutMapping("/{campeonatoId}/desabilitar-compartilhamento")
    @Operation(summary = "Desabilitar compartilhamento", description = "Desabilita o compartilhamento do campeonato")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compartilhamento desabilitado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Campeonato ou compartilhamento não encontrado")
    })
    public ResponseEntity<CompartilhamentoDto> desabilitarCompartilhamento(
            @Parameter(description = "UUID do campeonato") @PathVariable UUID campeonatoId) {
        return ResponseEntity.ok(compartilhamentoService.desabilitarCompartilhamento(campeonatoId));
    }

    @GetMapping("/{campeonatoId}/compartilhamento")
    @Operation(summary = "Buscar compartilhamento", description = "Busca o compartilhamento do campeonato")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compartilhamento encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Campeonato ou compartilhamento não encontrado")
    })
    public ResponseEntity<CompartilhamentoDto> buscarCompartilhamento(
            @Parameter(description = "UUID do campeonato") @PathVariable UUID campeonatoId) {
        return ResponseEntity.ok(compartilhamentoService.buscarCompartilhamento(campeonatoId));
    }
}
