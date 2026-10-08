package org.apemigos.eventos.repository;

import org.apemigos.eventos.entity.EventoCampo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventoCampoRepository extends JpaRepository<EventoCampo, Long> {

    @EntityGraph(attributePaths = "opcoes")
    List<EventoCampo> findByEventoIdOrderByOrdemAscIdAsc(Long eventoId);

    @EntityGraph(attributePaths = "opcoes")
    List<EventoCampo> findByEventoIdAndAtivoTrueOrderByOrdemAscIdAsc(Long eventoId);

    Optional<EventoCampo> findByEventoIdAndChave(Long eventoId, String chave);

    boolean existsByEventoIdAndChaveAndIdNot(Long eventoId, String chave, Long id);

    boolean existsByEventoIdAndChave(Long eventoId, String chave);
}
