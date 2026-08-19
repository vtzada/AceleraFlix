package vitortheof.com.br.aceleraflix.category.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "categoria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 80)
    private String nome;

    @Column(nullable = false, length = 80, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "criado_por", nullable = false)
    private UUID criadoPor;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @PrePersist
    protected void onCreate() {
        this.criadoEm = Instant.now();
    }
}
