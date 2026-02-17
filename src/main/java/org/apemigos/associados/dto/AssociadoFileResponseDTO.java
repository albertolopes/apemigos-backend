package org.apemigos.associados.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssociadoFileResponseDTO {
    private Long id;
    private Long associadoId;
    private String fieldName;
    private String originalName;
    private String contentType;
    private Long size;
    private String cloudPublicId;
    private String cloudUrl;
    private String cloudFolder;
    private Boolean cloudSuccess;
    private String cloudMessage;
    private LocalDateTime createdAt;
}
