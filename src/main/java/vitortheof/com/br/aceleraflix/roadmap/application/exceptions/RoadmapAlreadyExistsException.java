package vitortheof.com.br.aceleraflix.roadmap.application.exceptions;

public class RoadmapAlreadyExistsException extends RuntimeException {
    public RoadmapAlreadyExistsException(String message) {
        super(message);
    }
}
