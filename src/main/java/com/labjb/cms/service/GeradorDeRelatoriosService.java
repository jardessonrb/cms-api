package com.labjb.cms.service;

import com.labjb.cms.domain.dto.out.PontuacaoParcialDto;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class GeradorDeRelatoriosService {

    private final TemplateEngine templateEngine;
    private final FaseService faseService;

    public byte[] gerarRelatorio(UUID faseId) {

            List<PontuacaoParcialDto> pontuacaoParcialDtos = faseService.buscarPontuacaoParcial(faseId);
            Optional<PontuacaoParcialDto> primeiraPontuacao = pontuacaoParcialDtos.stream().findFirst();
            String categoria = primeiraPontuacao.isPresent() ? primeiraPontuacao.get().categoria() : "";
            String fase = primeiraPontuacao.isPresent() ? primeiraPontuacao.get().fase() : "";


        try {
            Context context = new Context();
            context.setVariable("dados", pontuacaoParcialDtos);
            context.setVariable("categoria", categoria);
            context.setVariable("fase", fase);

            String html = templateEngine.process("relatorio_ranking_fase", context);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();

            return outputStream.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao gerar PDF", e);
        }
    }
}
