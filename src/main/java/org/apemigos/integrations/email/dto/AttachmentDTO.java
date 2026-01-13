package org.apemigos.integrations.email.dto;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentDTO {
    private String content; // base64
    private String filename;
    private String type; // optional mime
}