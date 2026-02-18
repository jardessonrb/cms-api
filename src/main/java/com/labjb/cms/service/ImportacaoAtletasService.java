package com.labjb.cms.service;

import com.labjb.cms.domain.dto.out.AtletaImportacaoDto;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class ImportacaoAtletasService {

    public List<AtletaImportacaoDto> parseCsvFile(MultipartFile file) throws Exception {
        List<AtletaImportacaoDto> atletas = new ArrayList<>();
        DateTimeFormatter formatoLocalDateTime = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        DateTimeFormatter formatoLocalDate = DateTimeFormatter.ofPattern("yyyy/MM/dd");

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Pular cabeçalho
                }

                String[] values = line.split(",");
                if (values.length >= 11) {
                    LocalDateTime carimboDataHora = LocalDateTime.parse(values[0].trim(), formatoLocalDateTime);
                    String nomeCompleto = values[1].trim();
                    String apelido = values[2].trim();
                    String nomeApelidoProfessorMestre = values[3].trim();
                    LocalDate dataNascimento = LocalDate.parse(values[4].trim(), formatoLocalDate);
                    String graduacaoCorda = values[5].trim();
                    String cidade = values[6].trim();
                    String tamanhoCamisa = values[7].trim();
                    String sexo = values[8].trim();
                    String categoria = values[9].trim();
                    String grupoEscola = values[10].trim();

                    AtletaImportacaoDto atleta = new AtletaImportacaoDto(
                        carimboDataHora,
                        nomeCompleto,
                        apelido,
                        nomeApelidoProfessorMestre,
                        dataNascimento,
                        graduacaoCorda,
                        cidade,
                        tamanhoCamisa,
                        sexo,
                        categoria,
                        grupoEscola
                    );

                    atletas.add(atleta);
                }
            }
        }

        return atletas;
    }
}
