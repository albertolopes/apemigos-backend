package org.apemigos.eventos.entity;

import jakarta.persistence.*;
import lombok.*;
import org.apemigos.eventos.enums.EventoCampoTipo;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "evento_campo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoCampo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "chave", nullable = false)
    private String chave;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private EventoCampoTipo tipo;

    @Column(name = "obrigatorio", nullable = false)
    @Builder.Default
    private Boolean obrigatorio = false;

    @Column(name = "unico", nullable = false)
    @Builder.Default
    private Boolean unico = false;

    @Column(name = "ordem", nullable = false)
    @Builder.Default
    private Integer ordem = 0;

    @Column(name = "placeholder")
    private String placeholder;

    @Column(name = "texto_ajuda")
    private String textoAjuda;

    @Column(name = "ativo", nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "campo", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC, id ASC")
    @Builder.Default
    private List<EventoCampoOpcao> opcoes = new ArrayList<>();
}
