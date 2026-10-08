package org.apemigos.eventos.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apemigos.eventos.dto.*;
import org.apemigos.eventos.entity.*;
import org.apemigos.eventos.enums.EventoInscricaoStatus;
import org.apemigos.eventos.enums.EventoStatus;
import org.apemigos.eventos.repository.EventoInscricaoRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EventoMapperService {

    private final ObjectMapper objectMapper;
    private final EventoInscricaoRepository inscricaoRepository;

    public EventoDTO toDto(Evento evento, boolean includeCampos) {
        EventoDTO dto = EventoDTO.builder()
                .id(evento.getId())
                .titulo(evento.getTitulo())
                .slug(evento.getSlug())
                .descricao(evento.getDescricao())
                .imagem(evento.getImagem())
                .local(evento.getLocal())
                .dataInicio(evento.getDataInicio())
                .dataFim(evento.getDataFim())
                .inicioInscricoes(evento.getInicioInscricoes())
                .fimInscricoes(evento.getFimInscricoes())
                .limiteInscricoes(evento.getLimiteInscricoes())
                .status(evento.getStatus())
                .createdAt(evento.getCreatedAt())
                .updatedAt(evento.getUpdatedAt())
                .totalInscricoes(inscricaoRepository.countByEventoIdAndStatus(evento.getId(), EventoInscricaoStatus.CONFIRMADA))
                .build();
        if (includeCampos) {
            dto.setCampos(evento.getCampos().stream().map(this::toCampoDto).toList());
        }
        return dto;
    }

    public EventoPublicoDTO toPublicoDto(Evento evento, List<EventoCampo> campos, boolean inscricoesAbertas) {
        return EventoPublicoDTO.builder()
                .id(evento.getId())
                .titulo(evento.getTitulo())
                .slug(evento.getSlug())
                .descricao(evento.getDescricao())
                .imagem(evento.getImagem())
                .local(evento.getLocal())
                .dataInicio(evento.getDataInicio())
                .dataFim(evento.getDataFim())
                .inicioInscricoes(evento.getInicioInscricoes())
                .fimInscricoes(evento.getFimInscricoes())
                .limiteInscricoes(evento.getLimiteInscricoes())
                .inscricoesAbertas(inscricoesAbertas)
                .campos(campos.stream().map(this::toCampoDto).toList())
                .build();
    }

    public EventoCampoDTO toCampoDto(EventoCampo campo) {
        return EventoCampoDTO.builder()
                .id(campo.getId())
                .label(campo.getLabel())
                .chave(campo.getChave())
                .tipo(campo.getTipo())
                .obrigatorio(campo.getObrigatorio())
                .unico(campo.getUnico())
                .ordem(campo.getOrdem())
                .placeholder(campo.getPlaceholder())
                .textoAjuda(campo.getTextoAjuda())
                .ativo(campo.getAtivo())
                .createdAt(campo.getCreatedAt())
                .updatedAt(campo.getUpdatedAt())
                .opcoes(campo.getOpcoes().stream().map(this::toOpcaoDto).toList())
                .build();
    }

    public EventoCampoOpcaoDTO toOpcaoDto(EventoCampoOpcao opcao) {
        return EventoCampoOpcaoDTO.builder()
                .id(opcao.getId())
                .label(opcao.getLabel())
                .valor(opcao.getValor())
                .ordem(opcao.getOrdem())
                .build();
    }

    public EventoInscricaoDTO toInscricaoDto(EventoInscricao inscricao, boolean includeRespostas) {
        EventoInscricaoDTO dto = EventoInscricaoDTO.builder()
                .id(inscricao.getId())
                .eventoId(inscricao.getEvento().getId())
                .status(inscricao.getStatus())
                .nomeInscrito(inscricao.getNomeInscrito())
                .emailInscrito(inscricao.getEmailInscrito())
                .telefoneInscrito(inscricao.getTelefoneInscrito())
                .cpfInscrito(inscricao.getCpfInscrito())
                .createdAt(inscricao.getCreatedAt())
                .updatedAt(inscricao.getUpdatedAt())
                .build();
        if (includeRespostas) {
            dto.setRespostas(inscricao.getRespostas().stream().map(this::toRespostaDto).toList());
        }
        return dto;
    }

    public EventoInscricaoRespostaDTO toRespostaDto(EventoInscricaoResposta resposta) {
        return EventoInscricaoRespostaDTO.builder()
                .id(resposta.getId())
                .campoId(resposta.getCampo().getId())
                .chaveCampo(resposta.getChaveCampo())
                .label(resposta.getCampo().getLabel())
                .valor(resolveValor(resposta))
                .build();
    }

    private Object resolveValor(EventoInscricaoResposta resposta) {
        if (resposta.getValorJson() == null || resposta.getValorJson().isBlank()) {
            return resposta.getValorTexto();
        }
        try {
            return objectMapper.readValue(resposta.getValorJson(), Object.class);
        } catch (JsonProcessingException e) {
            return resposta.getValorJson();
        }
    }
}
