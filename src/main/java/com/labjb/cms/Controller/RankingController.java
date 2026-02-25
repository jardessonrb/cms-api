package com.labjb.cms.Controller;

import com.labjb.cms.domain.dto.out.PontuacaoParcialDto;
import com.labjb.cms.service.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ranking")
@Tag(name = "Ranking", description = "Operações de visualização de ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping("/visualizacao")
    @Operation(summary = "Visualizar ranking por token", description = "Visualiza ranking do campeonato usando token de compartilhamento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ranking retornado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Token não fornecido"),
            @ApiResponse(responseCode = "404", description = "Token inválido ou compartilhamento não habilitado")
    })
    public ResponseEntity<List<PontuacaoParcialDto>> visualizarRanking(
            @Parameter(description = "Token de compartilhamento do campeonato") 
            @RequestParam String token) {

        List<PontuacaoParcialDto> ranking = rankingService.buscarRankingPorToken(token);
        return ResponseEntity.ok(ranking);
    }
}
