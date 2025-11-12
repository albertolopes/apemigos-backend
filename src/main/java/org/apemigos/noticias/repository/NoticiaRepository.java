package org.apemigos.noticias.repository;

import org.apemigos.noticias.entity.Noticia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NoticiaRepository extends JpaRepository<Noticia, Long> {

    @Query("SELECT n FROM Noticia n WHERE " +
           "LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(n.shortDescription) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(n.slug) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Noticia> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    Optional<Noticia> findBySlug(String slug);

    Page<Noticia> findAllByOrderByDateDesc(Pageable pageable);
}