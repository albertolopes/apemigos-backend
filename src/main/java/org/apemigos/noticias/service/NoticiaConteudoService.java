package org.apemigos.noticias.service;

import lombok.RequiredArgsConstructor;
import org.apemigos.exceptions.ObjectNotFoundException;
import org.apemigos.noticias.dto.NoticiaConteudoDTO;
import org.apemigos.noticias.entity.Noticia;
import org.apemigos.noticias.entity.NoticiaConteudo;
import org.apemigos.noticias.mapper.NoticiaConteudoMapper;
import org.apemigos.noticias.repository.NoticiaConteudoRepository;
import org.apemigos.noticias.repository.NoticiaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NoticiaConteudoService {

    private final NoticiaConteudoRepository noticiaConteudoRepository;
    private final NoticiaRepository noticiaRepository;
    private final NoticiaConteudoMapper noticiaConteudoMapper;

    @Transactional
    public NoticiaConteudoDTO findByNoticiaId(Long noticiaId) {
        NoticiaConteudo conteudo = noticiaConteudoRepository.findByNoticiaId(noticiaId)
                .orElseThrow(() -> new ObjectNotFoundException("Conteúdo não encontrado para a notícia"));
        noticiaConteudoRepository.incrementTotalBuscas(conteudo.getId());
        return noticiaConteudoMapper.toDto(conteudo);
    }

    @Transactional
    public NoticiaConteudoDTO findByNoticiaSlug(String slug) {
        NoticiaConteudo conteudo = noticiaConteudoRepository.findByNoticiaSlug(slug)
                .orElseThrow(() -> new ObjectNotFoundException("Conteúdo não encontrado para a notícia"));
        noticiaConteudoRepository.incrementTotalBuscas(conteudo.getId());
        return noticiaConteudoMapper.toDto(conteudo);
    }

    public Integer findTotalBuscasByNoticiaId(Long noticiaId) {
        return noticiaConteudoRepository.findTotalBuscasByNoticiaId(noticiaId)
                .orElseThrow(() -> new ObjectNotFoundException("Conteúdo não encontrado para a notícia"));
    }

    @Transactional
    public NoticiaConteudoDTO save(NoticiaConteudoDTO conteudoDTO) {
        Noticia noticia = noticiaRepository.findById(conteudoDTO.getNoticia().getId())
                .orElseThrow(() -> new ObjectNotFoundException("Notícia não encontrada"));

        noticiaConteudoRepository.findByNoticiaId(conteudoDTO.getNoticia().getId())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Já existe conteúdo para esta notícia");
                });

        NoticiaConteudo conteudo = noticiaConteudoMapper.toEntity(conteudoDTO);
        conteudo.setNoticia(noticia);
        conteudo.setCreatedAt(LocalDateTime.now());
        conteudo.setUpdatedAt(LocalDateTime.now());
        conteudo.setTotalBuscas(0);

        NoticiaConteudo saved = noticiaConteudoRepository.save(conteudo);
        return noticiaConteudoMapper.toDto(saved);
    }

    @Transactional
    public NoticiaConteudoDTO update(Long noticiaId, NoticiaConteudoDTO conteudoDTO) {
        NoticiaConteudo existingConteudo = noticiaConteudoRepository.findByNoticiaId(noticiaId)
                .orElseThrow(() -> new ObjectNotFoundException("Conteúdo não encontrado"));

        existingConteudo.setLongDescription(conteudoDTO.getLongDescription());
        existingConteudo.setUpdatedAt(LocalDateTime.now());

        NoticiaConteudo updated = noticiaConteudoRepository.save(existingConteudo);
        return noticiaConteudoMapper.toDto(updated);
    }

    @Transactional
    public NoticiaConteudoDTO updateByNoticiaId(Long noticiaId, String longDescription) {
        NoticiaConteudo existingConteudo = noticiaConteudoRepository.findByNoticiaId(noticiaId)
                .orElseThrow(() -> new ObjectNotFoundException("Conteúdo não encontrado"));

        existingConteudo.setLongDescription(longDescription);
        existingConteudo.setUpdatedAt(LocalDateTime.now());

        NoticiaConteudo updated = noticiaConteudoRepository.save(existingConteudo);
        return noticiaConteudoMapper.toDto(updated);
    }

    @Transactional
    public void delete(Long id) {
        if (!noticiaConteudoRepository.existsById(id)) {
            throw new ObjectNotFoundException("Conteúdo não encontrado");
        }
        noticiaConteudoRepository.deleteById(id);
    }

    @Transactional
    public void deleteByNoticiaId(Long noticiaId) {
        if (!noticiaConteudoRepository.existsByNoticiaId(noticiaId)) {
            throw new ObjectNotFoundException("Conteúdo não encontrado para a notícia");
        }
        noticiaConteudoRepository.deleteByNoticiaId(noticiaId);
    }

    public boolean existsByNoticiaId(Long noticiaId) {
        return noticiaConteudoRepository.existsByNoticiaId(noticiaId);
    }
}
