package com.labjb.cms.Controller;

import com.labjb.cms.domain.dto.in.FaseForm;
import com.labjb.cms.domain.dto.out.FaseDto;
import com.labjb.cms.domain.dto.out.PontuacaoParcialDto;
import com.labjb.cms.domain.dto.out.ValidacaoCorteDto;
import com.labjb.cms.service.FaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
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

    @GetMapping("/{faseId}")
    @Operation(summary = "Buscar fase por UUID", description = "Busca uma fase específica pelo seu UUID")
    public ResponseEntity<FaseDto> buscarFasePorUuid(
            @Parameter(description = "UUID da fase") @PathVariable UUID faseId) {
        return ResponseEntity.ok(faseService.buscarFasePorUuid(faseId));
    }

    @GetMapping("/categoria/{categoriaId}")
    @Operation(summary = "Listar fases por categoria", description = "Lista todas as fases de uma categoria específica paginadas")
    public ResponseEntity<Page<FaseDto>> listarFasesPorCategoria(
            @Parameter(description = "UUID da categoria") @PathVariable UUID categoriaId,
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        return ResponseEntity.ok(faseService.listarFasesPorCategoria(categoriaId, pageable));
    }

    @GetMapping("/{faseId}/pontuacao-parcial")
    @Operation(summary = "Visualizar pontuação parcial", description = "Retorna a pontuação parcial dos atletas em uma fase específica")
    public ResponseEntity<List<PontuacaoParcialDto>> visualizarPontuacaoParcial(
            @Parameter(description = "UUID da fase") @PathVariable UUID faseId) {
        return ResponseEntity.ok(faseService.buscarPontuacaoParcial(faseId));
    }

    @PutMapping("/{faseId}/finalizar")
    @Operation(summary = "Finalizar fase", description = "Finaliza uma fase e salva os resultados dos atletas")
    public ResponseEntity<FaseDto> finalizarFase(
            @Parameter(description = "UUID da fase") @PathVariable UUID faseId) {
        return ResponseEntity.ok(faseService.finalizarFase(faseId));
    }

    @PostMapping("/fase-anterior/{faseAnteriorId}/atletas/{quantidadeAtletas}/validar-corte")
    @Operation(summary = "Validar corte de fase", description = "Valida se há empates na posição de corte para criação de nova fase")
    public ResponseEntity<ValidacaoCorteDto> validarCorte(
            @Parameter(description = "UUID da fase anterior") @PathVariable(name = "faseAnteriorId") UUID faseAnteriorId,
            @Parameter(description = "Quantidade de atletas para o corte") @PathVariable(name = "quantidadeAtletas") Integer quantidadeAtletas) {
        return ResponseEntity.ok(faseService.validaCorte(faseAnteriorId, quantidadeAtletas));
    }
}
