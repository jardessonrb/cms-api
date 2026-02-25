package com.labjb.cms.service;

import com.labjb.cms.component.Criptografia;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.labjb.cms.domain.dto.out.PontuacaoParcialDto;
import com.labjb.cms.domain.model.Campeonato;
import com.labjb.cms.domain.model.Compartilhamento;
import com.labjb.cms.domain.model.Fase;
import com.labjb.cms.repository.CampeonatoRepository;
import com.labjb.cms.repository.CompartilhamentoRepository;
import com.labjb.cms.repository.FaseRepository;
import com.labjb.cms.shared.errors.exception.RegraNegocioException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final Criptografia criptografia;
    private final CampeonatoRepository campeonatoRepository;
    private final CompartilhamentoRepository compartilhamentoRepository;
    private final FaseRepository faseRepository;
    private final FaseService faseService;

    public String extrairCampeonatoIdDoToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token é obrigatório");
        }

        try {
            String chave = "giEANblv8peAPPH07TefR5uMSBtqZ1j7";
            String descriptografado = criptografia.decripta(chave, token);
            
            ObjectMapper objectMapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> conteudo = objectMapper.readValue(descriptografado, Map.class);
            
            String campeonatoId = (String) conteudo.get("campeonatoId");
            
            if (campeonatoId == null) {
                throw new RuntimeException("Token inválido");
            }
            
            return campeonatoId;
            
        } catch (Exception e) {
            throw new RuntimeException("Token inválido");
        }
    }

    public List<PontuacaoParcialDto> buscarRankingPorToken(String token) {
        // Extrair campeonatoId do token
        String campeonatoIdStr = extrairCampeonatoIdDoToken(token);
        UUID campeonatoUuid = UUID.fromString(campeonatoIdStr);
        
        // Buscar campeonato
        Campeonato campeonato = campeonatoRepository.findByUuid(campeonatoUuid)
                .orElseThrow(() -> new EntityNotFoundException("Campeonato não encontrado"));
        
        // Verificar se o compartilhamento está habilitado
        Compartilhamento compartilhamento = compartilhamentoRepository.findByCampeonatoId(campeonato.getId())
                .orElseThrow(() -> new EntityNotFoundException("Compartilhamento não encontrado"));
        
        if (!compartilhamento.getIsHabilitado()) {
            throw new RegraNegocioException("Compartilhamento não está habilitado");
        }
        
        // Buscar fase compartilhada do campeonato
        Fase faseCompartilhada = faseRepository.findCompartilhadaPorCampeonatoId(campeonato.getId())
                .orElseThrow(() -> new EntityNotFoundException("Nenhuma fase compartilhada encontrada"));
        
        // Buscar ranking da fase usando o método existente
        return faseService.buscarPontuacaoParcial(faseCompartilhada.getUuid());
    }
}
