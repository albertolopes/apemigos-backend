package org.apemigos.eventos.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoCampoOpcaoDTO {
    private Long id;
    private String label;
    private String valor;
    private Integer ordem;
}
