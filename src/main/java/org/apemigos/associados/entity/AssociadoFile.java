package org.apemigos.associados.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Entity
@Table(name = "associado_file")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AssociadoFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "associado_id", nullable = false)
    private Associado associado;

    @Column(name = "field_name")
    private String fieldName;

    @Column(name = "original_name")
    private String originalName;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "size")
    private Long size;

    @Column(name = "cloud_public_id")
    private String cloudPublicId;

    @Column(name = "cloud_url", columnDefinition = "TEXT")
    private String cloudUrl;

    @Column(name = "cloud_folder")
    private String cloudFolder;

    @Column(name = "cloud_success")
    private Boolean cloudSuccess;

    @Column(name = "cloud_message", columnDefinition = "TEXT")
    private String cloudMessage;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}


