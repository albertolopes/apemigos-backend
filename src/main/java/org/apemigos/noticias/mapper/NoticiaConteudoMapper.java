package org.apemigos.noticias.mapper;


import org.apemigos.noticias.dto.NoticiaConteudoDTO;
import org.apemigos.noticias.entity.NoticiaConteudo;
import org.apemigos.share.mapper.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NoticiaConteudoMapper extends BaseMapper<NoticiaConteudo, NoticiaConteudoDTO> {
}
