package com.labjb.cms.Controller;

import com.labjb.cms.service.GeradorDeRelatoriosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/relatorios")
@Tag(name = "Relatórios", description = "Operações relacionadas a geração de relatórios")
public class GeradorDeRelatoriosController {

    private final GeradorDeRelatoriosService geradorDeRelatoriosService;

    @GetMapping("/fase/{faseId}/download-ranking-pdf")
    @Operation(summary = "Gerar relatório de ranking da fase", description = "Gera um PDF com o ranking de pontuação de uma fase específica")
    public ResponseEntity<?> gerarRelatorioRankingDaFase(@Parameter(description = "UUID da fase") @PathVariable(name = "faseId") UUID faseId) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_ranking_fase.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(geradorDeRelatoriosService.gerarRelatorioRanking(faseId));
    }

    @GetMapping("/atletas/fase/{faseId}/download-competidores-pdf")
    @Operation(summary = "Gerar relatório de competidores da fase", description = "Gera um PDF com a lista de competidores de uma fase específica")
    public ResponseEntity<?> gerarRelatorioAtletasDaFase(@Parameter(description = "UUID da fase") @PathVariable(name = "faseId") UUID faseId) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_competidores_fase.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(geradorDeRelatoriosService.gerarRelatorioAtletasFase(faseId));
    }

    @GetMapping("/categoria/{categoriaId}/download-ranking-geral-pdf")
    @Operation(summary = "Gerar relatório de ranking geral da categoria", description = "Gera um PDF com o ranking geral de pontuação de uma categoria específica")
    public ResponseEntity<?> gerarRelatorioRankingDaCategoria(@Parameter(description = "UUID da categoria") @PathVariable(name = "categoriaId") UUID categoriaId) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_ranking_categoria.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(geradorDeRelatoriosService.gerarRelatorioRankingCategoria(categoriaId));
    }

    @GetMapping("/fase/{faseId}/download-disputas-pdf")
    @Operation(summary = "Gerar relatório de disputas da fase", description = "Gera um PDF com as disputas agrupadas por fase")
    public ResponseEntity<?> gerarRelatorioDisputasDaFase(@Parameter(description = "UUID da fase") @PathVariable(name = "faseId") UUID faseId) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_disputas_fase.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(geradorDeRelatoriosService.gerarRelatorioDisputasPorFase(faseId));
    }
}
