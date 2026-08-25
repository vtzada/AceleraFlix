package vitortheof.com.br.aceleraflix.shared.infrastructure;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitingService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolverBucket(String ip) {
        return cache.computeIfAbsent(ip, this::criarBucket);
    }

    private Bucket criarBucket(String ip) {
        Bandwidth limite = Bandwidth.builder()
                .capacity(60)
                .refillIntervally(60, Duration.ofMinutes(1))
                .build();

        return Bucket.builder()
                .addLimit(limite)
                .build();
    }
}
