package org.apemigos.noticias.repository;

import org.apemigos.noticias.entity.Noticia;
import org.apemigos.noticias.enums.NoticiaStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoticiaRepository extends JpaRepository<Noticia, Long> {

    @Query("SELECT n FROM Noticia n WHERE " +
           "(:keyword IS NULL OR LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(n.shortDescription) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(n.slug) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "n.status IN :statuses")
    Page<Noticia> findByKeywordAndStatusIn(@Param("keyword") String keyword, @Param("statuses") List<NoticiaStatus> statuses, Pageable pageable);

    Optional<Noticia> findBySlug(String slug);

    // Busca case-insensitive - útil quando o usuário envia o slug sem garantir case
    Optional<Noticia> findBySlugIgnoreCase(String slug);

    Page<Noticia> findAllByStatusInOrderByDateDesc(List<NoticiaStatus> statuses, Pageable pageable);
}