package org.apemigos.eventos.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoPublicoDTO {
    private Long id;
    private String titulo;
    private String slug;
    private String descricao;
    private String imagem;
    private String local;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private LocalDateTime inicioInscricoes;
    private LocalDateTime fimInscricoes;
    private Integer limiteInscricoes;
    private Boolean inscricoesAbertas;
    @Builder.Default
    private List<EventoCampoDTO> campos = new ArrayList<>();
}
