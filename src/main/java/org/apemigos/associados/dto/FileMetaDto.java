package org.apemigos.associados.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileMetaDto {
    private String field;
    private String name;
    private String contentType;
    private Long size;
}

