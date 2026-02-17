package org.apemigos.associados.mapper;

import org.apemigos.associados.dto.AssociadoFileResponseDTO;
import org.apemigos.associados.entity.AssociadoFile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AssociadoFileMapper {

    @Mapping(source = "associado.id", target = "associadoId")
    AssociadoFileResponseDTO toDto(AssociadoFile entity);

    List<AssociadoFileResponseDTO> toDtoList(List<AssociadoFile> entities);
}
