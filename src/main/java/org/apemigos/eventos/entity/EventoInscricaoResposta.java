package org.apemigos.eventos.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "evento_inscricao_resposta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoInscricaoResposta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inscricao_id", nullable = false)
    private EventoInscricao inscricao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campo_id", nullable = false)
    private EventoCampo campo;

    @Column(name = "chave_campo", nullable = false)
    private String chaveCampo;

    @Column(name = "valor_texto", columnDefinition = "TEXT")
    private String valorTexto;

    @Column(name = "valor_json", columnDefinition = "TEXT")
    private String valorJson;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
