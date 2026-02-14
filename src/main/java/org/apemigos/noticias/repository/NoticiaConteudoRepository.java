package org.apemigos.noticias.repository;

import org.apemigos.noticias.entity.NoticiaConteudo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NoticiaConteudoRepository extends JpaRepository<NoticiaConteudo, Long> {
    
    Optional<NoticiaConteudo> findByNoticiaId(Long noticiaId);
    
    @Query("SELECT nc FROM NoticiaConteudo nc WHERE nc.noticia.slug = :slug")
    Optional<NoticiaConteudo> findByNoticiaSlug(@Param("slug") String slug);
    
    boolean existsByNoticiaId(Long noticiaId);
    
    void deleteByNoticiaId(Long noticiaId);

    @Modifying
    @Query("UPDATE NoticiaConteudo nc SET nc.totalBuscas = nc.totalBuscas + 1 WHERE nc.id = :id")
    void incrementTotalBuscas(@Param("id") Long id);
}