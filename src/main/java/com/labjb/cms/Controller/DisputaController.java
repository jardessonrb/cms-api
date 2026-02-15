package com.labjb.cms.Controller;

import com.labjb.cms.domain.dto.in.DisputaForm;
import com.labjb.cms.domain.dto.in.RegistrarNotasForm;
import com.labjb.cms.domain.dto.out.DisputaDto;
import com.labjb.cms.service.DisputaService;
import com.labjb.cms.service.NotaService;
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
@RequestMapping("/disputa")
@Tag(name = "Disputas", description = "Operações relacionadas as disputas das rodadas")
public class DisputaController {

    private final DisputaService disputaService;
    private final NotaService notaService;

    @GetMapping("/rodada/{rodadaId}")
    @Operation(summary = "Listar disputas por rodada", description = "Lista disputas de uma rodada específica ordenadas por data de criação descendente")
    public ResponseEntity<List<DisputaDto>> listarDisputasPorRodada(
            @Parameter(description = "UUID da rodada") @PathVariable UUID rodadaId){
        return ResponseEntity.ok(disputaService.listarDisputasPorRodada(rodadaId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar disputa por UUID", description = "Busca uma disputa específica pelo seu UUID")
    public ResponseEntity<DisputaDto> buscarDisputaPorUuid(
            @Parameter(description = "UUID da disputa") @PathVariable UUID id) {
        return ResponseEntity.ok(disputaService.buscarDisputaPorUuid(id));
    }

    @PostMapping("/rodada/{rodadaId}")
    @Operation(summary = "Criar disputa manual", description = "Cria uma nova disputa manual para uma rodada específica")
    public ResponseEntity<DisputaDto> criarDisputaManual(
            @Parameter(description = "UUID da rodada") @PathVariable UUID rodadaId,
            @RequestBody DisputaForm disputaForm,
            UriComponentsBuilder uriComponentsBuilder) {
        DisputaDto disputaDto = disputaService.criarDisputaManual(rodadaId, disputaForm);
        URI uri = uriComponentsBuilder.path("/disputa/{id}").buildAndExpand(disputaDto.id()).toUri();
        return ResponseEntity.created(uri).body(disputaDto);
    }

    @PostMapping("/{disputaId}/registrar-notas")
    @Operation(summary = "Registrar notas da disputa", description = "Registra as notas dos jurados para os atletas de uma disputa")
    public ResponseEntity<DisputaDto> registrarNotas(
            @Parameter(description = "UUID da disputa") @PathVariable UUID disputaId,
            @RequestBody RegistrarNotasForm registrarNotasForm) {
        return ResponseEntity.ok(notaService.registrarNotas(disputaId, registrarNotasForm));
    }

    @PutMapping("/{disputaId}/atualizar-notas")
    @Operation(summary = "Atualizar notas da disputa", description = "Atualiza as notas dos jurados para os atletas de uma disputa existente")
    public ResponseEntity<DisputaDto> atualizarNotas(
            @Parameter(description = "UUID da disputa") @PathVariable UUID disputaId,
            @RequestBody RegistrarNotasForm registrarNotasForm) {
        return ResponseEntity.ok(notaService.atualizaNotasDisputa(disputaId, registrarNotasForm));
    }
}
