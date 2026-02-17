package org.apemigos.associados.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apemigos.associados.enums.StatusCarteirinha;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssociadoResponseDTO {
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
    private String observacoes;
    private LocalDateTime createdAt;
    private StatusCarteirinha statusCarteirinha;
}
