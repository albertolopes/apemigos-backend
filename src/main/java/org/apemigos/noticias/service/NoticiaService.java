package org.apemigos.noticias.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apemigos.exceptions.ObjectNotFoundException;
import org.apemigos.noticias.dto.NoticiaDTO;
import org.apemigos.noticias.entity.Noticia;
import org.apemigos.noticias.enums.NoticiaStatus;
import org.apemigos.noticias.mapper.NoticiaMapper;
import org.apemigos.noticias.repository.NoticiaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticiaService {

    private final NoticiaRepository noticiaRepository;
    private final NoticiaMapper noticiaMapper;
    private final HttpServletRequest request;

    public Page<NoticiaDTO> findAll(Pageable pageable) {
        List<NoticiaStatus> statuses = getStatusesBasedOnAuth();
        Page<Noticia> noticias = noticiaRepository.findAllByStatusInOrderByDateDesc(statuses, pageable);
        return noticias.map(noticiaMapper::toDto);
    }

    public Page<NoticiaDTO> findByKeyword(String keyword, Pageable pageable) {
        List<NoticiaStatus> statuses = getStatusesBasedOnAuth();
        Page<Noticia> noticias = noticiaRepository.findByKeywordAndStatusIn(keyword, statuses, pageable);
        return noticias.map(noticiaMapper::toDto);
    }

    private List<NoticiaStatus> getStatusesBasedOnAuth() {
        String serviceToken = request.getHeader("X-Service-Token");
        if (serviceToken != null && !serviceToken.isBlank()) {
            return List.of(NoticiaStatus.APROVADO);
        }
        return List.of(NoticiaStatus.APROVADO, NoticiaStatus.PENDENTE);
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
                    if (noticiaDetails.getStatus() != null) {
                        noticia.setStatus(noticiaDetails.getStatus());
                    }
                    return noticiaRepository.save(noticia);
                })
                .orElseThrow(() -> new ObjectNotFoundException("Noticia não encontrada"))
        );

    }

    public NoticiaDTO updateStatus(Long id, NoticiaStatus status) {
        if (status == NoticiaStatus.EXCLUIDO) {
            throw new IllegalArgumentException("Para excluir utilize o endpoint de delete");
        }
        return noticiaMapper.toDto(
                noticiaRepository.findById(id)
                        .map(noticia -> {
                            noticia.setStatus(status);
                            return noticiaRepository.save(noticia);
                        })
                        .orElseThrow(() -> new ObjectNotFoundException("Noticia não encontrada"))
        );
    }

    public boolean delete(Long id) {
        return noticiaRepository.findById(id).map(noticia -> {
            noticia.setStatus(NoticiaStatus.EXCLUIDO);
            noticiaRepository.save(noticia);
            return true;
        }).orElse(false);
    }
}