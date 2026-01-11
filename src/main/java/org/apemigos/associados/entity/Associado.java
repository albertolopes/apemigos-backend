package org.apemigos.associados.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@Table(name = "associado")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Associado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String sobrenome;
    private LocalDate dataNascimento;
    private String cpf;
    private String rg;
    private String email;
    private String telefoneContato;
    private String telefoneEmergencia;
    private String nomeContatoEmergencia;
    private String medicoResponsavel;
    private String telefoneMedico;
    private Boolean possuiConvenio;
    private String convenioNome;

    private String cidade;
    private String estado;
    private String bairro;
    private String logradouro;
    private String complemento;
    private String cep;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "associado", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AssociadoFile> files = new ArrayList<>();
}
