package org.apemigos.projetos.service;

import lombok.RequiredArgsConstructor;
import org.apemigos.exceptions.ObjectNotFoundException;
import org.apemigos.projetos.dto.ProjetoDTO;
import org.apemigos.projetos.entity.Projeto;
import org.apemigos.projetos.mapper.ProjetoMapper;
import org.apemigos.projetos.repository.ProjetoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjetoService {

    private final ProjetoRepository projetoRepository;
    private final ProjetoMapper projetoMapper;

    public Page<ProjetoDTO> findAll(Pageable pageable) {
        Page<Projeto> projetos = projetoRepository.findAllByOrderByIdDesc(pageable);
        return projetos.map(projetoMapper::toDto);
    }

    public Page<ProjetoDTO> findByKeyword(String keyword, Pageable pageable) {
        Page<Projeto> projetos = projetoRepository.findByKeyword(keyword, pageable);
        return projetos.map(projetoMapper::toDto);
    }

    public ProjetoDTO findById(Long id) {
        return projetoMapper.toDto(
                projetoRepository.findById(id)
                        .orElseThrow(() -> new ObjectNotFoundException("Projeto não encontrado"))
        );
    }

    public ProjetoDTO findBySlug(String slug) {
        return projetoMapper.toDto(
                projetoRepository.findBySlug(slug)
                        .orElseThrow(() -> new ObjectNotFoundException("Projeto não encontrado"))
        );
    }

    public ProjetoDTO save(ProjetoDTO projeto) {
        return projetoMapper.toDto(
                projetoRepository.save(
                        projetoMapper.toEntity(
                                projeto
                        )
                )
        );
    }

    public ProjetoDTO update(ProjetoDTO projetoDetails) {
        return projetoMapper.toDto(
                projetoRepository.findById(projetoDetails.getId())
                        .map(projeto -> {
                            projeto.setTitle(projetoDetails.getTitle());
                            projeto.setDescription(projetoDetails.getDescription());
                            projeto.setCover(projetoDetails.getCover());
                            projeto.setShortDescription(projetoDetails.getShortDescription());
                            projeto.setSlug(projetoDetails.getSlug());
                            return projetoRepository.save(projeto);
                        })
                        .orElseThrow(() -> new ObjectNotFoundException("Projeto não encontrado"))
        );

    }

    public boolean delete(Long id) {
        if (projetoRepository.existsById(id)) {
            projetoRepository.deleteById(id);
            return true;
        }
        return false;
    }
}

