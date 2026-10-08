package org.apemigos.eventos.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoInscricaoRequestDTO {
    @Builder.Default
    private List<EventoInscricaoRespostaRequestDTO> respostas = new ArrayList<>();
}
