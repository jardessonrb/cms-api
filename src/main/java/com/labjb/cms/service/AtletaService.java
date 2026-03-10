package com.labjb.cms.service;

import com.labjb.cms.domain.dto.in.AtletaForm;
import com.labjb.cms.domain.dto.in.CategoriaForm;
import com.labjb.cms.domain.dto.out.AtletaDto;
import com.labjb.cms.domain.dto.out.AtletaImportacaoDto;
import com.labjb.cms.domain.dto.out.CategoriaDto;
import com.labjb.cms.domain.dto.out.RetornoImportacaoAtletasDto;
import com.labjb.cms.domain.enums.SituacaoAtletaEnum;
import com.labjb.cms.domain.enums.SituacaoCategoriaEnum;
import com.labjb.cms.domain.enums.SituacaoInscricaoCategoriaEnum;
import com.labjb.cms.domain.model.Atleta;
import com.labjb.cms.domain.model.Campeonato;
import com.labjb.cms.domain.model.Categoria;
import com.labjb.cms.domain.model.InscricaoCategoria;
import com.labjb.cms.repository.AtletaRepository;
import com.labjb.cms.repository.CampeonatoRepository;
import com.labjb.cms.repository.CategoriaRepository;
import com.labjb.cms.repository.InscricaoCategoriaRepository;
import com.labjb.cms.shared.errors.exception.RegraNegocioException;
import com.labjb.cms.shared.mapper.AtletaMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RequiredArgsConstructor
@Service
public class AtletaService {

    private final AtletaRepository atletaRepository;
    private final CategoriaRepository categoriaRepository;
    private final InscricaoCategoriaRepository inscricaoCategoriaRepository;
    private final CampeonatoRepository campeonatoRepository;
    private final AtletaMapper atletaMapper;
    private final ImportacaoAtletasService importacaoAtletasService;
    private final CategoriaService categoriaService;

    public AtletaDto criaAtleta(AtletaForm atletaForm) {
        Campeonato campeonato = campeonatoRepository.findByUuid(atletaForm.campeonatoId())
                .orElseThrow(() -> new EntityNotFoundException("Campeonato não encontrado"));

        // Validar se o número já existe no campeonato
        if (atletaForm.numero() != null) {
            atletaRepository.findByNumeroAndCampeonatoUuid(atletaForm.numero(), atletaForm.campeonatoId())
                    .ifPresent(atleta -> {
                        throw new RegraNegocioException("Já existe um atleta com o número " + atletaForm.numero() + " neste campeonato");
                    });
        }

        Integer maximoNumeroPorCampeonatoUuid = atletaRepository.findMaxNumeroByCampeonatoUuid(campeonato.getUuid());
        Atleta atleta = atletaMapper.toEntity(atletaForm);
        atleta.setCampeonato(campeonato);
        atleta.setNumero(atletaForm.numero() == null || atleta.getNumero() == 0 ? (maximoNumeroPorCampeonatoUuid + 1) : atletaForm.numero());
        atleta = atletaRepository.save(atleta);

        if(Objects.nonNull(atletaForm.categoriaId())){
            inscreveAtletaNaCategoria(atletaForm, atleta);
        }

        return atletaMapper.toDto(atleta);
    }

    private void inscreveAtletaNaCategoria(AtletaForm atletaForm, Atleta atleta) {
        Categoria categoria = categoriaRepository
                .findByUuidAndCampeonatoUuid(atletaForm.categoriaId(), atletaForm.campeonatoId())
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada no campeonato para o id informado"));

        Optional<InscricaoCategoria> inscricaoCategoria = inscricaoCategoriaRepository.findByAtletaAndCategoria(atleta.getUuid(), categoria.getUuid());
        if(inscricaoCategoria.isPresent()){
            return;
        }

        InscricaoCategoria inscricao = InscricaoCategoria.builder()
                .atleta(atleta)
                .categoria(categoria)
                .situacao(SituacaoInscricaoCategoriaEnum.ATIVO)
                .build();
        inscricaoCategoriaRepository.save(inscricao);
    }

    private void atualizaInscricaoDeAtletaNaCategoria(AtletaForm atletaForm, Atleta atleta) {
        Categoria categoria = categoriaRepository
                .findByUuidAndCampeonatoUuid(atletaForm.categoriaId(), atletaForm.campeonatoId())
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada no campeonato para o id informado"));

        Optional<InscricaoCategoria> inscricaoCategoria = atleta.getInscricoesCategoria().stream().findFirst();
        if(inscricaoCategoria.isPresent()){
            InscricaoCategoria inscricaoCategoriaAtual = inscricaoCategoria.get();
            inscricaoCategoriaAtual.setCategoria(categoria);
            inscricaoCategoriaRepository.save(inscricaoCategoriaAtual);
        }else{
            InscricaoCategoria inscricao = InscricaoCategoria.builder()
                    .atleta(atleta)
                    .categoria(categoria)
                    .situacao(SituacaoInscricaoCategoriaEnum.ATIVO)
                    .build();
            inscricaoCategoriaRepository.save(inscricao);
        }
    }

    @Transactional
    public AtletaDto atualizaAtleta(UUID id, AtletaForm atletaForm) {
        Atleta atleta = atletaRepository.findByUuid(id)
                .orElseThrow(() -> new RuntimeException("Atleta não encontrado"));

        // Validar se o número já existe no campeonato (apenas se for diferente do atual)
        if (atletaForm.numero() != null && !atletaForm.numero().equals(atleta.getNumero())) {
            atletaRepository.findByNumeroAndCampeonatoUuid(atletaForm.numero(), atleta.getCampeonato().getUuid())
                    .ifPresent(atletaExistente -> {
                        throw new RegraNegocioException("Já existe um atleta com o número " + atletaForm.numero() + " neste campeonato");
                    });
        }

        atleta.setNome(atletaForm.nome());
        atleta.setNumero(atletaForm.numero());
        atleta.setApelido(atletaForm.apelido());
        atleta.setResponsavel(atletaForm.responsavel());
        atleta.setDataNascimento(atletaForm.dataNascimento());
        atleta.setCidade(atletaForm.cidade());
        atleta.setGrupo(atletaForm.grupo());
        atleta.setGraduacao(atletaForm.graduacao());

        if(Objects.nonNull(atletaForm.categoriaId())){
            atualizaInscricaoDeAtletaNaCategoria(atletaForm, atleta);
        }

        return atletaMapper.toDto(atletaRepository.save(atleta));
    }

    public Page<AtletaDto> listarAtletas(String filtro, UUID campeonatoId, SituacaoAtletaEnum situacao, Pageable pageable) {
        return atletaRepository.findAllWithFilter(filtro, campeonatoId, situacao, pageable)
                .map(atletaMapper::toDto);
    }

    public Page<AtletaDto> listarAtletasPorCategoria(String filtro, UUID categoriaId, SituacaoAtletaEnum situacao, Pageable pageable) {
        return atletaRepository.findAllByCategoriaWithFilter(filtro, categoriaId, situacao, pageable)
                .map(atletaMapper::toDto);
    }

    public Page<AtletaDto> listarAtletasPorFase(String filtro, UUID faseId, SituacaoAtletaEnum situacao, Pageable pageable) {
        return atletaRepository.findAllByFaseWithFilter(filtro, faseId, situacao, pageable)
                .map(atletaMapper::toDto);
    }

    public AtletaDto visualizarAtleta(UUID id) {
        return atletaRepository.findByUuid(id)
                .map(atletaMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Atleta não encontrado"));
    }

    @Transactional
//    @Async
    public RetornoImportacaoAtletasDto processaArquivoImportacaoAtletasCSV(UUID campeonatoId, MultipartFile file) {
        Campeonato campeonato = campeonatoRepository.findByUuid(campeonatoId)
                .orElseThrow(() -> new EntityNotFoundException("Campeonato não encontrado"));

        List<AtletaImportacaoDto> atletas = importarAtletas(file);
        Map<String, Categoria> categorias = new HashMap<>();
        int contadorDeAtletasCriados = 0;
        int contadorDeCategoriasCriadas = 0;
        for(AtletaImportacaoDto atletaImportacao : atletas){

            try{
                if(!categorias.containsKey(atletaImportacao.categoria())){
                    Optional<Categoria> categoriaPorNomeOpt = categoriaRepository.findByCampeonatoIdAndNomeIgnoreCase(campeonato.getId(), atletaImportacao.categoria());
                    if(categoriaPorNomeOpt.isPresent()){
                        categorias.put(atletaImportacao.categoria(), categoriaPorNomeOpt.get());
                    }else{
                        Categoria novaCategoria = new Categoria();
                        novaCategoria.setCampeonato(campeonato);
                        novaCategoria.setNome(atletaImportacao.categoria());
                        novaCategoria.setSituacao(SituacaoCategoriaEnum.CRIADA);
                        novaCategoria = categoriaRepository.save(novaCategoria);
                        categorias.put(atletaImportacao.categoria(), novaCategoria);
                        contadorDeCategoriasCriadas++;
                    }
                }

                AtletaForm atletaForm = new AtletaForm(
                        atletaImportacao.nomeCompleto(),
                        atletaRepository.findMaxNumeroByCampeonatoUuid(campeonatoId) + 1,
                        atletaImportacao.apelido(),
                        atletaImportacao.nomeApelidoProfessorMestre(),
                        atletaImportacao.dataNascimento(),
                        atletaImportacao.cidade(),
                        atletaImportacao.grupoEscola(),
                        atletaImportacao.graduacaoCorda(),
                        campeonato.getUuid(),
                        categorias.get(atletaImportacao.categoria()).getUuid()
                );

                criaAtleta(atletaForm);
                contadorDeAtletasCriados++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return new RetornoImportacaoAtletasDto(contadorDeCategoriasCriadas, contadorDeAtletasCriados, atletas.size());
    }

    public CategoriaDto criaCategoriaPorNome(CategoriaForm categoriaForm){
        return categoriaService.criaCategoria(categoriaForm);
    }

    public List<AtletaImportacaoDto> importarAtletas(MultipartFile file) {
        validarArquivoImportacao(file);
        
        try {
            return importacaoAtletasService.parseCsvFile(file);
        } catch (Exception e) {
            throw new RegraNegocioException("Erro ao processar arquivo CSV: " + e.getMessage());
        }
    }

    @Transactional
    public void cancelarAtleta(UUID atletaUuid) {
        Atleta atleta = atletaRepository.findByUuid(atletaUuid)
                .orElseThrow(() -> new EntityNotFoundException("Atleta não encontrado"));

        if (atleta.getSituacao() == SituacaoAtletaEnum.CANCELADO) {
            throw new RegraNegocioException("Atleta já está cancelado");
        }

        atleta.setSituacao(SituacaoAtletaEnum.CANCELADO);
        atletaRepository.save(atleta);
        atletaRepository.atualizaRegistroDisputaParaNaoPontuado(atleta.getId());
        atletaRepository.atualizaDisputaConformeTipoRegistroDisputa(atleta.getId());
    }

    private void validarArquivoImportacao(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RegraNegocioException("Arquivo não pode estar vazio");
        }

        if (!file.getOriginalFilename().endsWith(".csv")) {
            throw new RegraNegocioException("Arquivo deve ter extensão .csv");
        }
    }
}
