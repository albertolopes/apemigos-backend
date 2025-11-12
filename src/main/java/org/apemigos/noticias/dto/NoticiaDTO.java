package org.apemigos.noticias.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
public class NoticiaDTO {

    private Long id;
    
    private String image;

    private String title;

    private LocalDateTime date;

    private String shortDescription;
    
    private String slug;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;

}