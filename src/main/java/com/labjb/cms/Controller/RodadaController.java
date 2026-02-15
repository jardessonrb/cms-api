package com.labjb.cms.Controller;

import com.labjb.cms.domain.dto.in.RodadaForm;
import com.labjb.cms.domain.dto.out.RodadaDto;
import com.labjb.cms.service.RodadaService;
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
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/rodada")
@Tag(name = "Rodadas", description = "Operações relacionadas a rodadas das competições")
public class RodadaController {

    private final RodadaService rodadaService;

    @PostMapping
    @Operation(summary = "Criar nova rodada", description = "Cria uma nova rodada vinculada a uma fase")
    public ResponseEntity<RodadaDto> criaRodada(@RequestBody RodadaForm rodadaForm, UriComponentsBuilder uriComponentsBuilder) {
        RodadaDto rodadaDto = rodadaService.criaRodada(rodadaForm);
        URI uri = uriComponentsBuilder.path("/rodada/{id}").buildAndExpand(rodadaDto.id()).toUri();
        return ResponseEntity.created(uri).body(rodadaDto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar rodada", description = "Atualiza os dados de uma rodada existente")
    public ResponseEntity<RodadaDto> atualizaRodada(
            @Parameter(description = "UUID da rodada") @PathVariable UUID id,
            @RequestBody RodadaForm rodadaForm) {
        return ResponseEntity.ok(rodadaService.atualizaRodada(id, rodadaForm));
    }

    @GetMapping("/fase/{faseId}")
    @Operation(summary = "Listar rodadas por fase", description = "Lista rodadas de uma fase específica ordenadas por data de criação descendente, com filtro opcional por nome da fase")
    public ResponseEntity<Page<RodadaDto>> listarRodadasPorFase(
            @Parameter(description = "UUID da fase") @PathVariable UUID faseId,
            @Parameter(description = "Filtro para busca por nome da fase") @RequestParam(required = false) String filtro,
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        return ResponseEntity.ok(rodadaService.listarRodadasPorFase(faseId, filtro, pageable));
    }

    @PostMapping("/fase/{faseId}/gerar-rodadas")
    @Operation(summary = "Gerar múltiplas rodadas para uma fase", description = "Cria múltiplas rodadas para uma fase específica")
    public ResponseEntity<List<RodadaDto>> geraRodadasParaFase(
            @Parameter(description = "UUID da fase") @PathVariable UUID faseId,
            @RequestBody List<RodadaForm> rodadasForms) {
        return ResponseEntity.ok(rodadaService.geraRodadasParaFase(faseId, rodadasForms));
    }

    @PutMapping("/{id}/finalizar")
    @Operation(summary = "Finalizar rodada", description = "Finaliza uma rodada existente")
    public ResponseEntity<RodadaDto> finalizarRodada(
            @Parameter(description = "UUID da rodada") @PathVariable UUID id) {
        return ResponseEntity.ok(rodadaService.finalizarRodada(id));
    }
}
