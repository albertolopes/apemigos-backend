package org.apemigos.eventos.dto;

import lombok.*;
import org.apemigos.eventos.enums.EventoStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoDTO {
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
    private EventoStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long totalInscricoes;
    @Builder.Default
    private List<EventoCampoDTO> campos = new ArrayList<>();
}
