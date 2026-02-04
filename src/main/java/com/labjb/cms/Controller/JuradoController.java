package com.labjb.cms.Controller;

import com.labjb.cms.domain.dto.in.JuradoForm;
import com.labjb.cms.domain.dto.out.JuradoDto;
import com.labjb.cms.service.JuradoService;
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
@RequestMapping("/jurado")
@Tag(name = "Jurados", description = "Operações relacionadas aos jurados das competições")
public class JuradoController {

    private final JuradoService juradoService;

    @PostMapping
    @Operation(summary = "Criar novo jurado", description = "Cria um novo jurado vinculado a um campeonato")
    public ResponseEntity<JuradoDto> criaJurado(@RequestBody JuradoForm juradoForm, UriComponentsBuilder uriComponentsBuilder) {
        JuradoDto juradoDto = juradoService.criaJurado(juradoForm);
        URI uri = uriComponentsBuilder.path("/jurado/{id}").buildAndExpand(juradoDto.id()).toUri();
        return ResponseEntity.created(uri).body(juradoDto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar jurado", description = "Atualiza os dados de um jurado existente")
    public ResponseEntity<JuradoDto> atualizaJurado(
            @Parameter(description = "UUID do jurado") @PathVariable UUID id,
            @RequestBody JuradoForm juradoForm) {
        return ResponseEntity.ok(juradoService.atualizaJurado(id, juradoForm));
    }

    @GetMapping("/campeonato/{campeonatoId}")
    @Operation(summary = "Listar jurados por campeonato", description = "Lista jurados de um campeonato específico com filtro opcional")
    public ResponseEntity<Page<JuradoDto>> listarJuradosPorCampeonato(
            @Parameter(description = "UUID do campeonato") @PathVariable UUID campeonatoId,
            @Parameter(description = "Filtro para busca por nome, apelido ou grupo") @RequestParam(required = false) String filtro,
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        return ResponseEntity.ok(juradoService.listarJuradosPorCampeonato(campeonatoId, filtro, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar jurado por UUID", description = "Busca um jurado específico pelo seu UUID")
    public ResponseEntity<JuradoDto> buscarJuradoPorUuid(
            @Parameter(description = "UUID do jurado") @PathVariable UUID id) {
        return ResponseEntity.ok(juradoService.buscarJuradoPorUuid(id));
    }
}
