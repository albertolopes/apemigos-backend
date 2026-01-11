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

    @Query(value = "SELECT n.* FROM noticia n WHERE (:keyword IS NULL OR LOWER(n.title) LIKE '%' || LOWER(CAST(:keyword AS text)) || '%' OR LOWER(n.short_description) LIKE '%' || LOWER(CAST(:keyword AS text)) || '%' OR LOWER(n.slug) LIKE '%' || LOWER(CAST(:keyword AS text)) || '%')",
           countQuery = "SELECT count(*) FROM noticia n WHERE (:keyword IS NULL OR LOWER(n.title) LIKE '%' || LOWER(CAST(:keyword AS text)) || '%' OR LOWER(n.short_description) LIKE '%' || LOWER(CAST(:keyword AS text)) || '%' OR LOWER(n.slug) LIKE '%' || LOWER(CAST(:keyword AS text)) || '%')",
           nativeQuery = true
    )
    Page<Noticia> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    Optional<Noticia> findBySlug(String slug);

    // Busca case-insensitive - útil quando o usuário envia o slug sem garantir case
    Optional<Noticia> findBySlugIgnoreCase(String slug);

    Page<Noticia> findAllByOrderByDateDesc(Pageable pageable);
}