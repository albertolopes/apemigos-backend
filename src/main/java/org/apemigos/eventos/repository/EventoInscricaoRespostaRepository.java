package org.apemigos.eventos.repository;

import org.apemigos.eventos.entity.EventoInscricaoResposta;
import org.apemigos.eventos.enums.EventoInscricaoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventoInscricaoRespostaRepository extends JpaRepository<EventoInscricaoResposta, Long> {

    boolean existsByCampoId(Long campoId);

    @Query("""
            SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
            FROM EventoInscricaoResposta r
            WHERE r.campo.id = :campoId
              AND LOWER(r.valorTexto) = LOWER(:valorTexto)
              AND r.inscricao.evento.id = :eventoId
              AND r.inscricao.status = :status
            """)
    boolean existsUniqueValue(@Param("eventoId") Long eventoId,
                              @Param("campoId") Long campoId,
                              @Param("valorTexto") String valorTexto,
                              @Param("status") EventoInscricaoStatus status);
}
