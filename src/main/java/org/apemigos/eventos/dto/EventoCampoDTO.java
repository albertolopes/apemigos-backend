package org.apemigos.eventos.dto;

import lombok.*;
import org.apemigos.eventos.enums.EventoCampoTipo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoCampoDTO {
    private Long id;
    private String label;
    private String chave;
    private EventoCampoTipo tipo;
    private Boolean obrigatorio;
    private Boolean unico;
    private Integer ordem;
    private String placeholder;
    private String textoAjuda;
    private Boolean ativo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Builder.Default
    private List<EventoCampoOpcaoDTO> opcoes = new ArrayList<>();
}
