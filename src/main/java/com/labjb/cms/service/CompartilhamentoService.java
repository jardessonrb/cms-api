package com.labjb.cms.service;

import com.labjb.cms.component.Criptografia;
import com.labjb.cms.domain.dto.out.CompartilhamentoDto;
import com.labjb.cms.domain.model.Campeonato;
import com.labjb.cms.domain.model.Compartilhamento;
import com.labjb.cms.domain.model.User;
import com.labjb.cms.repository.CampeonatoRepository;
import com.labjb.cms.repository.CompartilhamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompartilhamentoService {

    private final CompartilhamentoRepository compartilhamentoRepository;
    private final CampeonatoRepository campeonatoRepository;
    private final AuthService authService;
    private final Criptografia criptografia;

    public CompartilhamentoDto criarCompartilhamento(UUID campeonatoUuid) {
        User usuarioAutenticado = authService.obtemUsuarioAutenticado();
        
        Campeonato campeonato = campeonatoRepository.findByUuid(campeonatoUuid)
                .orElseThrow(() -> new RuntimeException("Campeonato não encontrado"));

        // Validar se o campeonato pertence ao grupo do usuário
        if (!campeonato.getGrupo().getId().equals(usuarioAutenticado.getGrupo().getId())) {
            throw new RuntimeException("Campeonato não encontrado");
        }

        // Verificar se já existe compartilhamento
        if (compartilhamentoRepository.findByCampeonatoId(campeonato.getId()).isPresent()) {
            throw new RuntimeException("Compartilhamento já existe para este campeonato");
        }

        // Gerar token
        String token = gerarToken(campeonato);

        // Criar compartilhamento
        Compartilhamento compartilhamento = Compartilhamento.builder()
                .campeonato(campeonato)
                .token(token)
                .isHabilitado(false)
                .build();

        compartilhamentoRepository.save(compartilhamento);

        return new CompartilhamentoDto(false, token);
    }

    public CompartilhamentoDto habilitarCompartilhamento(UUID campeonatoUuid) {
        User usuarioAutenticado = authService.obtemUsuarioAutenticado();
        
        Campeonato campeonato = campeonatoRepository.findByUuid(campeonatoUuid)
                .orElseThrow(() -> new RuntimeException("Campeonato não encontrado"));

        // Validar se o campeonato pertence ao grupo do usuário
        if (!campeonato.getGrupo().getId().equals(usuarioAutenticado.getGrupo().getId())) {
            throw new RuntimeException("Campeonato não encontrado");
        }

        Compartilhamento compartilhamento = compartilhamentoRepository.findByCampeonatoId(campeonato.getId())
                .orElseThrow(() -> new RuntimeException("Compartilhamento não encontrado"));

        compartilhamento.setIsHabilitado(true);
        compartilhamentoRepository.save(compartilhamento);

        return new CompartilhamentoDto(true, compartilhamento.getToken());
    }

    public CompartilhamentoDto desabilitarCompartilhamento(UUID campeonatoUuid) {
        User usuarioAutenticado = authService.obtemUsuarioAutenticado();
        
        Campeonato campeonato = campeonatoRepository.findByUuid(campeonatoUuid)
                .orElseThrow(() -> new RuntimeException("Campeonato não encontrado"));

        // Validar se o campeonato pertence ao grupo do usuário
        if (!campeonato.getGrupo().getId().equals(usuarioAutenticado.getGrupo().getId())) {
            throw new RuntimeException("Campeonato não encontrado");
        }

        Compartilhamento compartilhamento = compartilhamentoRepository.findByCampeonatoId(campeonato.getId())
                .orElseThrow(() -> new RuntimeException("Compartilhamento não encontrado"));

        compartilhamento.setIsHabilitado(false);
        compartilhamentoRepository.save(compartilhamento);

        return new CompartilhamentoDto(false, compartilhamento.getToken());
    }

    public CompartilhamentoDto buscarCompartilhamento(UUID campeonatoUuid) {
        User usuarioAutenticado = authService.obtemUsuarioAutenticado();
        
        Campeonato campeonato = campeonatoRepository.findByUuid(campeonatoUuid)
                .orElseThrow(() -> new RuntimeException("Campeonato não encontrado"));

        // Validar se o campeonato pertence ao grupo do usuário
        if (!campeonato.getGrupo().getId().equals(usuarioAutenticado.getGrupo().getId())) {
            throw new RuntimeException("Campeonato não encontrado");
        }

        Compartilhamento compartilhamento = compartilhamentoRepository.findByCampeonatoId(campeonato.getId())
                .orElseThrow(() -> new RuntimeException("Compartilhamento não encontrado"));

        return new CompartilhamentoDto(compartilhamento.getIsHabilitado(), compartilhamento.getToken());
    }

    private String gerarToken(Campeonato campeonato) {
        String chave = "giEANblv8peAPPH07TefR5uMSBtqZ1j7"; // Chave fixa como no exemplo
        
        Map<String, Object> conteudo = new HashMap<>();
        conteudo.put("campeonatoId", campeonato.getUuid().toString());
        conteudo.put("timestamp", System.currentTimeMillis());
        
        try {
            String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(conteudo);
            return criptografia.encripta(chave, json);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar token", e);
        }
    }
}
