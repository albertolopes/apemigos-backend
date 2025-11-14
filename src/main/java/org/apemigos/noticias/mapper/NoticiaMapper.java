package org.apemigos.noticias.mapper;

import org.apemigos.noticias.dto.NoticiaDTO;
import org.apemigos.noticias.entity.Noticia;
import org.apemigos.util.mapper.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NoticiaMapper extends BaseMapper<Noticia, NoticiaDTO> {

}
