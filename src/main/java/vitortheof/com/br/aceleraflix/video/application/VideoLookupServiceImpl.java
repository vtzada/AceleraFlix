package vitortheof.com.br.aceleraflix.video.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vitortheof.com.br.aceleraflix.video.infrastructure.VideoRepository;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class VideoLookupServiceImpl implements VideoLookupService {

    private final VideoRepository videoRepository;

    @Override
    public boolean existsById(UUID id) {
        return videoRepository.existsById(id);
    }
}
