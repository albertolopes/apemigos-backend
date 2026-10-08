package org.apemigos.eventos.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoInscricaoRespostaRequestDTO {
    private Long campoId;
    private String chaveCampo;
    private Object valor;
}
