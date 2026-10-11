package vitortheof.com.br.aceleraflix.video.application;

import java.util.UUID;

public interface VideoLookupService {

    boolean existsById(UUID id);
}
