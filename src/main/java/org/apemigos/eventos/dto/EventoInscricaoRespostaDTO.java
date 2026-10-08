package org.apemigos.eventos.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoInscricaoRespostaDTO {
    private Long id;
    private Long campoId;
    private String chaveCampo;
    private String label;
    private Object valor;
}
