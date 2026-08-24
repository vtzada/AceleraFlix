package vitortheof.com.br.aceleraflix.shared.infrastructure;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
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
                .capacity(30)
                .refillIntervally(30, Duration.ofMinutes(1))
                .build();

        return Bucket.builder()
                .addLimit(limite)
                .build();
    }
}
