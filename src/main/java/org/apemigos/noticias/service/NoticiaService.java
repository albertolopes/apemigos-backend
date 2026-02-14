package org.apemigos.noticias.service;

import lombok.RequiredArgsConstructor;
import org.apemigos.exceptions.ObjectNotFoundException;
import org.apemigos.noticias.dto.NoticiaDTO;
import org.apemigos.noticias.entity.Noticia;
import org.apemigos.noticias.mapper.NoticiaMapper;
import org.apemigos.noticias.repository.NoticiaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.text.Normalizer;

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
                noticiaRepository.findBySlugIgnoreCase(slug)
                        .orElseThrow(() -> new ObjectNotFoundException("Noticia não encontrada"))
        );
    }

    public NoticiaDTO save(NoticiaDTO noticia) {
        String slug = Normalizer.normalize(noticia.getTitle(), Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-");

        if (slug.length() > 10)
            slug = slug.substring(0, 10);

        noticia.setSlug(slug.replaceAll("-$", ""));

        return noticiaMapper.toDto(
                        noticiaRepository.save(
                                noticiaMapper.toEntity(
                                        noticia
                                )
                        )
                );
    }

    public NoticiaDTO update(Long id, NoticiaDTO noticiaDetails) {
        return noticiaMapper.toDto(
                noticiaRepository.findById(id)
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