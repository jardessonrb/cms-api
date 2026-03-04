package com.labjb.cms.Controller;

import com.labjb.cms.service.GeradorDeRelatoriosService;
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
public class GeradorDeRelatoriosController {

    private final GeradorDeRelatoriosService geradorDeRelatoriosService;

    @GetMapping("/fase/{faseId}/download-ranking-pdf")
    public ResponseEntity<?> gerarRelatorioRankingDaFase(@PathVariable(name = "faseId") UUID faseId) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_ranking_fase.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(geradorDeRelatoriosService.gerarRelatorio(faseId));
    }
}
