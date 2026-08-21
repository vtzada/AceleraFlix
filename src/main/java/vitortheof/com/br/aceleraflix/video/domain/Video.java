package vitortheof.com.br.aceleraflix.video.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "video")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 160)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "url_embed", nullable = false)
    private String urlEmbed;

    @Column(name = "video_externo_id", nullable = false, length = 30)
    private String videoExternoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Plataforma plataforma = Plataforma.YOUTUBE;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "duracao_segundos")
    private Integer duracaoSegundos;

    @Column(name = "categoria_id", nullable = true)
    private UUID categoriaId;

    @Column(name = "criado_por", nullable = false)
    private UUID criadoPor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusVideo status = StatusVideo.APROVADO;

    @ManyToMany
    @JoinTable(
            name = "video_tag",
            joinColumns =  @JoinColumn(name = "video_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    @Column(name = "criado_em")
    private Instant criadoEm;

    @Column(name = "atualizado_em")
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
