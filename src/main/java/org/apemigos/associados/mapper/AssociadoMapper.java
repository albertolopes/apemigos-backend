package org.apemigos.associados.mapper;

import org.apemigos.associados.dto.AssociadoRequisicaoDTO;
import org.apemigos.associados.dto.AssociadoResponseDTO;
import org.apemigos.associados.entity.Associado;
import org.apemigos.util.mapper.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.ArrayList;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class AssociadoMapper implements BaseMapper<Associado, AssociadoRequisicaoDTO> {

    @Override
    public Associado toEntity(AssociadoRequisicaoDTO dto) {
        Associado associado = Associado.builder()
                .nome(dto.getNome())
                .sobrenome(dto.getSobrenome())
                .dataNascimento(dto.getDataNascimento())
                .cpf(dto.getCpf())
                .rg(dto.getRg())
                .email(dto.getEmail())
                .telefoneContato(dto.getTelefoneContato())
                .nomeContatoEmergencia(dto.getNomeContatoEmergencia())
                .telefoneEmergencia(dto.getTelefoneEmergencia())
                .medicoResponsavel(dto.getMedicoResponsavel())
                .telefoneMedico(dto.getTelefoneMedico())
                .convenioNome(dto.getConvenioNome())
                .cidade(dto.getCidade())
                .estado(dto.getEstado())
                .bairro(dto.getBairro())
                .logradouro(dto.getLogradouro())
                .complemento(dto.getComplemento())
                .cep(dto.getCep())
                .observacoes(dto.getObservacoes())
                .build();

        if (associado.getFiles() == null)
            associado.setFiles(new ArrayList<>());

        associado.setPossuiConvenio(dto.getConvenioNome() != null && !dto.getConvenioNome().isBlank());
        return associado;
    }

    public abstract AssociadoResponseDTO toResponseDto(Associado entity);
}