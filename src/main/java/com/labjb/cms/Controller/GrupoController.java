package com.labjb.cms.Controller;

import com.labjb.cms.domain.dto.in.GrupoForm;
import com.labjb.cms.domain.dto.out.GrupoDto;
import com.labjb.cms.service.GrupoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/grupo")
@Tag(name = "Grupos", description = "Operações relacionadas a grupos")
public class GrupoController {

    private final GrupoService grupoService;

    @PostMapping
    @Operation(summary = "Criar novo grupo", description = "Cria um novo grupo no sistema com o usuário autenticado como proprietário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Grupo criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "403", description = "Usuário não autenticado")
    })
    public ResponseEntity<GrupoDto> criarGrupo(
            @RequestBody GrupoForm grupoForm, 
            UriComponentsBuilder uriComponentsBuilder) {
        GrupoDto grupoDto = grupoService.criarGrupo(grupoForm);
        URI uri = uriComponentsBuilder.path("/grupo/{id}").buildAndExpand(grupoDto.id()).toUri();
        return ResponseEntity.created(uri).body(grupoDto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar grupo por ID", description = "Busca um grupo específico pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Grupo encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Grupo não encontrado")
    })
    public ResponseEntity<GrupoDto> buscarGrupoPorId(
            @Parameter(description = "ID do grupo") @PathVariable UUID id) {
        return ResponseEntity.ok(grupoService.buscarGrupoPorId(id));
    }
}
