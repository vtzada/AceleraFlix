package vitortheof.com.br.aceleraflix.auth.application;

import vitortheof.com.br.aceleraflix.auth.application.dto.UserDTO;

import java.util.UUID;

public interface UserLookupService {
    UserDTO findById(UUID userId);
    boolean isEditorApproved(UUID userId);
}
