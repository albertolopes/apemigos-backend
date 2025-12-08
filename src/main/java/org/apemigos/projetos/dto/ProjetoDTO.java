package org.apemigos.projetos.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
public class ProjetoDTO {

    private Long id;

    private String title;

    private String description;

    private String cover;

    private String shortDescription;

    private String slug;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

