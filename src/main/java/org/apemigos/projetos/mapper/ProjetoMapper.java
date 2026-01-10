package org.apemigos.projetos.mapper;

import org.apemigos.projetos.dto.ProjetoDTO;
import org.apemigos.projetos.entity.Projeto;
import org.apemigos.util.mapper.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProjetoMapper extends BaseMapper<Projeto, ProjetoDTO> {

}

