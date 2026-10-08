package org.apemigos.eventos.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "evento_campo_opcao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoCampoOpcao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campo_id", nullable = false)
    private EventoCampo campo;

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "valor", nullable = false)
    private String valor;

    @Column(name = "ordem", nullable = false)
    @Builder.Default
    private Integer ordem = 0;
}
