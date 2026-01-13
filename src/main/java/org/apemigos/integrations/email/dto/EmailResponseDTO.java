package org.apemigos.integrations.email.dto;


import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmailResponseDTO {
    private String id;
    private String message;
}
