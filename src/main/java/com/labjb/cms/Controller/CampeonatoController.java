package com.labjb.cms.Controller;

import com.labjb.cms.domain.dto.in.CampeonatoForm;
import com.labjb.cms.domain.dto.out.CampeonatoDto;
import com.labjb.cms.service.CampeonatoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping("/campeonato")
@Tag(name = "Campeonatos", description = "Operações relacionadas a campeonatos")
public class CampeonatoController {

    private final CampeonatoService campeonatoService;

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
}
