package vitortheof.com.br.aceleraflix.roadmap.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "roadmap_resources")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoadmapResource {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;
    private Integer orderIndex;

    @Enumerated(EnumType.STRING)
    private ResourceType resourceType;

    @Column(name = "external_url")
    private String externalUrl;

    @Column(name = "video_id")
    private UUID videoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private RoadmapTopic topic;

}
