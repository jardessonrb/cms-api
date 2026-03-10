package com.labjb.cms.service;

import com.labjb.cms.domain.dto.out.AtletaDto;
import com.labjb.cms.domain.dto.out.FaseDto;
import com.labjb.cms.domain.dto.out.PontuacaoParcialDto;
import com.labjb.cms.domain.dto.out.PontuacaoGeralCategoriaDto;
import com.labjb.cms.domain.model.Fase;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class GeradorDeRelatoriosService {

    private final TemplateEngine templateEngine;
    private final FaseService faseService;
    private final AtletaService atletaService;
    private final CategoriaService categoriaService;

    public byte[] gerarRelatorioRanking(UUID faseId) {
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

    public byte[] gerarRelatorioAtletasFase(UUID faseId) {
        Fase fase = faseService.buscaFasePorUuid(faseId);

        List<AtletaDto> atletas = atletaService.listarAtletasPorFase(
                null,
                faseId,
                null,
                Pageable.unpaged()
        ).getContent();


        try {
            Context context = new Context();
            context.setVariable("dados", atletas);
            context.setVariable("categoria", fase.getCategoria().getNome());
            context.setVariable("fase", fase.getNome());

            String html = templateEngine.process("relatorio_competidores_fase", context);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();

            return outputStream.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao gerar PDF", e);
        }

    }

    public byte[] gerarRelatorioRankingCategoria(UUID categoriaId) {
        List<PontuacaoGeralCategoriaDto> pontuacaoGeralDtos = categoriaService.buscarPontuacaoGeralPorCategoria(categoriaId);
        Optional<PontuacaoGeralCategoriaDto> primeiraPontuacao = pontuacaoGeralDtos.stream().findFirst();
        String categoria = primeiraPontuacao.isPresent() ? primeiraPontuacao.get().categoria() : "";

        try {
            Context context = new Context();
            context.setVariable("dados", pontuacaoGeralDtos);
            context.setVariable("categoria", categoria);

            String html = templateEngine.process("relatorio_ranking_categoria", context);

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
