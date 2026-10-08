package org.apemigos.eventos.repository;

import org.apemigos.eventos.entity.EventoInscricao;
import org.apemigos.eventos.enums.EventoInscricaoStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EventoInscricaoRepository extends JpaRepository<EventoInscricao, Long> {

    long countByEventoIdAndStatus(Long eventoId, EventoInscricaoStatus status);

    @EntityGraph(attributePaths = {"respostas", "respostas.campo"})
    Optional<EventoInscricao> findByIdAndEventoId(Long id, Long eventoId);

    @Query("""
            SELECT DISTINCT i FROM EventoInscricao i
            LEFT JOIN i.respostas r
            WHERE i.evento.id = :eventoId
              AND (:keyword IS NULL OR :keyword = ''
                   OR LOWER(COALESCE(i.nomeInscrito, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(COALESCE(i.emailInscrito, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(COALESCE(i.telefoneInscrito, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(COALESCE(i.cpfInscrito, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(COALESCE(r.valorTexto, '')) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:cpf IS NULL OR :cpf = '' OR i.cpfInscrito LIKE CONCAT('%', :cpf, '%'))
              AND (:email IS NULL OR :email = '' OR LOWER(i.emailInscrito) LIKE LOWER(CONCAT('%', :email, '%')))
              AND (:telefone IS NULL OR :telefone = '' OR i.telefoneInscrito LIKE CONCAT('%', :telefone, '%'))
              AND (CAST(:dataInicio AS timestamp) IS NULL OR i.createdAt >= :dataInicio)
              AND (CAST(:dataFim AS timestamp) IS NULL OR i.createdAt <= :dataFim)
            """)
    Page<EventoInscricao> findByFiltros(@Param("eventoId") Long eventoId,
                                        @Param("keyword") String keyword,
                                        @Param("cpf") String cpf,
                                        @Param("email") String email,
                                        @Param("telefone") String telefone,
                                        @Param("dataInicio") LocalDateTime dataInicio,
                                        @Param("dataFim") LocalDateTime dataFim,
                                        Pageable pageable);
}
