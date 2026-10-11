package vitortheof.com.br.aceleraflix.video.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "avaliacao_video")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvaliacaoVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "video_id", nullable = false)
    private UUID videoId;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(nullable = false)
    private Integer nota;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    @PrePersist
    protected void onCreate() {
        this.criadoEm = Instant.now();
        this.atualizadoEm = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.atualizadoEm = Instant.now();
    }
}
