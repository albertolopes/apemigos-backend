package org.apemigos.eventos.repository;

import org.apemigos.eventos.entity.Evento;
import org.apemigos.eventos.enums.EventoStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EventoRepository extends JpaRepository<Evento, Long> {

    Page<Evento> findAllByStatus(EventoStatus status, Pageable pageable);

    Optional<Evento> findBySlugIgnoreCase(String slug);

    boolean existsBySlugIgnoreCase(String slug);

    boolean existsBySlugIgnoreCaseAndIdNot(String slug, Long id);

    @Query("""
            SELECT e FROM Evento e
            WHERE (:status IS NULL OR e.status = :status)
              AND (:keyword IS NULL OR :keyword = ''
                   OR LOWER(e.titulo) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(e.slug) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(COALESCE(e.local, '')) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Evento> findAdmin(@Param("status") EventoStatus status,
                           @Param("keyword") String keyword,
                           Pageable pageable);
}
