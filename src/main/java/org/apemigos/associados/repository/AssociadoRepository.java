package org.apemigos.associados.repository;

import org.apemigos.associados.entity.Associado;
import org.apemigos.associados.enums.StatusCarteirinha;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssociadoRepository extends JpaRepository<Associado, Long> {

    @Query("SELECT a FROM Associado a WHERE " +
           "(:status IS NULL OR a.statusCarteirinha = :status) AND " +
           "(COALESCE(:keyword, '') = '' OR " +
           "LOWER(a.nome) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) OR " +
           "LOWER(a.sobrenome) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) OR " +
           "LOWER(a.cpf) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) OR " +
           "LOWER(a.email) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) OR " +
           "LOWER(a.cidade) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')))")
    Page<Associado> findByFilters(@Param("status") StatusCarteirinha status,
                                  @Param("keyword") String keyword, 
                                  Pageable pageable);

    Optional<Associado> findByCpf(String cpf);
}
