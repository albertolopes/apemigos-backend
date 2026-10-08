package org.apemigos.eventos.entity;

import jakarta.persistence.*;
import lombok.*;
import org.apemigos.eventos.enums.EventoInscricaoStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "evento_inscricao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoInscricao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private EventoInscricaoStatus status = EventoInscricaoStatus.CONFIRMADA;

    @Column(name = "nome_inscrito")
    private String nomeInscrito;

    @Column(name = "email_inscrito")
    private String emailInscrito;

    @Column(name = "telefone_inscrito")
    private String telefoneInscrito;

    @Column(name = "cpf_inscrito")
    private String cpfInscrito;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "inscricao", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    @Builder.Default
    private List<EventoInscricaoResposta> respostas = new ArrayList<>();
}
