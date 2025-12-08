package org.apemigos.projetos.repository;

import org.apemigos.projetos.entity.Projeto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjetoRepository extends JpaRepository<Projeto, Long> {

    @Query("SELECT p FROM Projeto p WHERE " +
           "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.slug) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Projeto> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    Optional<Projeto> findBySlug(String slug);

    Page<Projeto> findAllByOrderByIdDesc(Pageable pageable);
}

