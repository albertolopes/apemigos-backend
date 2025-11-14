package org.apemigos.usuarios.repository;

import org.apemigos.usuarios.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Buscar usuário por email
    Optional<User> findByEmail(String email);
    
    // Buscar usuário por email que esteja ativo
    Optional<User> findByEmailAndIsActiveTrue(String email);
    
    // Verificar se email existe
    Boolean existsByEmail(String email);
    
    // Buscar todos os usuários ativos
    List<User> findByIsActiveTrue();
    
    // Buscar usuários por role
    List<User> findByRole(User.UserRole role);
    
    // Buscar usuários ativos por role
    List<User> findByRoleAndIsActiveTrue(User.UserRole role);
    
    // Buscar usuário por ID que esteja ativo
    Optional<User> findByIdAndIsActiveTrue(Long id);
    
    // Buscar usuários por nome (case insensitive)
    List<User> findByNameContainingIgnoreCase(String name);
    
    // Buscar usuários ativos por nome (case insensitive)
    List<User> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);
    
    // Buscar usuários com paginação (exemplo com query customizada)
    @Query("SELECT u FROM User u WHERE u.isActive = true ORDER BY u.createdAt DESC")
    List<User> findAllActiveUsers();
    
    // Contar usuários ativos
    Long countByIsActiveTrue();
    
    // Contar usuários por role
    Long countByRoleAndIsActiveTrue(User.UserRole role);
    
    // Buscar usuários criados após uma data específica
    List<User> findByCreatedAtAfterAndIsActiveTrue(java.time.LocalDateTime date);
    
    // Buscar usuário por email com query customizada
    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email) AND u.isActive = true")
    Optional<User> findActiveUserByEmailIgnoreCase(@Param("email") String email);
    
    // Verificar se existe usuário com email diferente do ID especificado (para update)
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :email AND u.id != :id")
    Boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") Long id);
}