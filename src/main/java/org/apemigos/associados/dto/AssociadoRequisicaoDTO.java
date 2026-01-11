package org.apemigos.associados.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "AssociateRequestDto", description = "Dados enviados pelo formulário de associado (multipart/form-data)")
public class AssociadoRequisicaoDTO {

    @NotBlank
    @Schema(description = "Nome", example = "Maria")
    private String nome;

    @Schema(description = "Sobrenome", example = "Silva")
    private String sobrenome;

    @Past
    @Schema(description = "Data de nascimento (YYYY-MM-DD)", example = "1980-07-15")
    private LocalDate dataNascimento;

    @NotBlank
    @Schema(description = "CPF", example = "52998224725")
    private String cpf;

    @Schema(description = "RG", example = "12.345.678-9")
    private String rg;

    @Email
    @Schema(description = "Email", example = "maria@example.com")
    private String email;

    @Schema(description = "Telefone de contato", example = "(61) 99999-9999")
    private String telefoneContato;

    @Schema(description = "Telefone de emergência", example = "(61) 98888-8888")
    private String telefoneEmergencia;

    @Schema(description = "Médico responsável", example = "Dr. João")
    private String medicoResponsavel;

    @Schema(description = "Telefone do médico", example = "(61) 3333-4444")
    private String telefoneMedico;

    @Schema(description = "Nome do convênio (se houver)", example = "Plano X")
    private String convenioNome;

    @Schema(description = "Cidade", example = "Brasília")
    private String cidade;

    @Schema(description = "Estado", example = "DF")
    private String estado;

    @Schema(description = "Bairro", example = "Asa Sul")
    private String bairro;

    @Schema(description = "Logradouro", example = "exemplo")
    private String logradouro;

    @Schema(description = "Complemento", example = "Apto 101")
    private String complemento;

    @Schema(description = "CEP", example = "70000-000")
    private String cep;

    @Schema(description = "Observações" , example = "Teste de associação")
    private String observacoes;

    @Schema(description = "HTML que deve ser enviado no corpo do e-mail (opcional)", example = "<p>Obrigado por se associar!</p>")
    private String bodyHtml;

    @Schema(description = "Arquivo do laudo (PDF)")
    private MultipartFile laudo;

    @Schema(description = "Foto 3x4 (imagem)")
    private MultipartFile foto3x4;

    @Schema(description = "Documento de identidade (PDF)")
    private MultipartFile documento;
}
