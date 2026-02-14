package org.apemigos.noticias.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "noticia_conteudo")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NoticiaConteudo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "noticia_id", nullable = false)
    private Noticia noticia;
    
    @Column(name = "long_description", nullable = false, columnDefinition = "TEXT")
    private String longDescription;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "total_buscas")
    private Integer totalBuscas;

}