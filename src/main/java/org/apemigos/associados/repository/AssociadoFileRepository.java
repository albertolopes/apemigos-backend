package org.apemigos.associados.repository;

import org.apemigos.associados.entity.AssociadoFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssociadoFileRepository extends JpaRepository<AssociadoFile, Long> {
    List<AssociadoFile> findByAssociadoId(Long associadoId);
}


