package org.apemigos.noticias.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
public class NoticiaConteudoDTO {
    private Long id;
    
    private NoticiaDTO noticia;
    
    private String longDescription;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;

    private Integer totalBuscas;
}