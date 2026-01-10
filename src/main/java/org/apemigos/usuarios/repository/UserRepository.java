package org.apemigos.usuarios.repository;

import org.apemigos.usuarios.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByEmailAndIsActiveTrue(String email);

    Boolean existsByEmail(String email);

    List<Usuario> findByIsActiveTrue();

    List<Usuario> findByRole(Usuario.UserRole role);

    List<Usuario> findByRoleAndIsActiveTrue(Usuario.UserRole role);

    Optional<Usuario> findByIdAndIsActiveTrue(Long id);

    List<Usuario> findByNomeContainingIgnoreCase(String nome);

    List<Usuario> findByNomeContainingIgnoreCaseAndIsActiveTrue(String nome);

    @Query("SELECT u FROM Usuario u WHERE u.isActive = true ORDER BY u.createdAt DESC")
    List<Usuario> findAllActiveUsers();

    Long countByIsActiveTrue();

    Long countByRoleAndIsActiveTrue(Usuario.UserRole role);

    List<Usuario> findByCreatedAtAfterAndIsActiveTrue(java.time.LocalDateTime date);

    @Query("SELECT u FROM Usuario u WHERE LOWER(u.email) = LOWER(:email) AND u.isActive = true")
    Optional<Usuario> findActiveUserByEmailIgnoreCase(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM Usuario u WHERE u.email = :email AND u.id != :id")
    Boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") Long id);
}