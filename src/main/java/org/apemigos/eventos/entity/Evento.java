package org.apemigos.eventos.entity;

import jakarta.persistence.*;
import lombok.*;
import org.apemigos.eventos.enums.EventoStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "evento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titulo", nullable = false)
    private String titulo;

    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "imagem", length = 500)
    private String imagem;

    @Column(name = "local")
    private String local;

    @Column(name = "data_inicio")
    private LocalDateTime dataInicio;

    @Column(name = "data_fim")
    private LocalDateTime dataFim;

    @Column(name = "inicio_inscricoes")
    private LocalDateTime inicioInscricoes;

    @Column(name = "fim_inscricoes")
    private LocalDateTime fimInscricoes;

    @Column(name = "limite_inscricoes")
    private Integer limiteInscricoes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private EventoStatus status = EventoStatus.RASCUNHO;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC, id ASC")
    @Builder.Default
    private List<EventoCampo> campos = new ArrayList<>();
}
