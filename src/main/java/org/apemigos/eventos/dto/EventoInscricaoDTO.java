package org.apemigos.eventos.dto;

import lombok.*;
import org.apemigos.eventos.enums.EventoInscricaoStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoInscricaoDTO {
    private Long id;
    private Long eventoId;
    private EventoInscricaoStatus status;
    private String nomeInscrito;
    private String emailInscrito;
    private String telefoneInscrito;
    private String cpfInscrito;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Builder.Default
    private List<EventoInscricaoRespostaDTO> respostas = new ArrayList<>();
}
