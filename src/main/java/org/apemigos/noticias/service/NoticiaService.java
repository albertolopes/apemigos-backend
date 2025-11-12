package org.apemigos.noticias.service;

import lombok.RequiredArgsConstructor;
import org.apemigos.exception.ObjectNotFoundException;
import org.apemigos.noticias.dto.NoticiaDTO;
import org.apemigos.noticias.entity.Noticia;
import org.apemigos.noticias.mapper.NoticiaMapper;
import org.apemigos.noticias.repository.NoticiaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NoticiaService {

    private final NoticiaRepository noticiaRepository;
    private final NoticiaMapper noticiaMapper;

    public Page<NoticiaDTO> findAll(Pageable pageable) {
        Page<Noticia> noticias = noticiaRepository.findAllByOrderByDateDesc(pageable);
        return noticias.map(noticiaMapper::toDto);
    }
    
    public Page<NoticiaDTO> findByKeyword(String keyword, Pageable pageable) {
        Page<Noticia> noticias = noticiaRepository.findByKeyword(keyword, pageable);
        return noticias.map(noticiaMapper::toDto);
    }
    
    public NoticiaDTO findById(Long id) {
        return noticiaMapper.toDto(
                noticiaRepository.findById(id)
                        .orElseThrow(() -> new ObjectNotFoundException("Noticia não encontrada"))
        );
    }
    
    public NoticiaDTO findBySlug(String slug) {
        return noticiaMapper.toDto(
                noticiaRepository.findBySlug(slug)
                        .orElseThrow(() -> new ObjectNotFoundException("Noticia não encontrada"))
        );
    }
    
    public NoticiaDTO save(NoticiaDTO noticia) {
        return noticiaMapper.toDto(
                        noticiaRepository.save(
                                noticiaMapper.toEntity(
                                        noticia
                                )
                        )
                );
    }
    
    public NoticiaDTO update(NoticiaDTO noticiaDetails) {
        return noticiaMapper.toDto(
                noticiaRepository.findById(noticiaDetails.getId())
                .map(noticia -> {
                    noticia.setTitle(noticiaDetails.getTitle());
                    noticia.setShortDescription(noticiaDetails.getShortDescription());
                    noticia.setImage(noticiaDetails.getImage());
                    noticia.setDate(noticiaDetails.getDate());
                    noticia.setSlug(noticiaDetails.getSlug());
                    return noticiaRepository.save(noticia);
                })
                .orElseThrow(() -> new ObjectNotFoundException("Noticia não encontrada"))
        );

    }
    
    public boolean delete(Long id) {
        if (noticiaRepository.existsById(id)) {
            noticiaRepository.deleteById(id);
            return true;
        }
        return false;
    }
}