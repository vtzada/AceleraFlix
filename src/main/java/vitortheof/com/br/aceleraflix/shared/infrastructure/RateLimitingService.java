package vitortheof.com.br.aceleraflix.shared.infrastructure;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RateLimitingService {
    public enum Policy { READ, WRITE, AUTH, REFRESH }

    private record Key(String ip, Policy policy) {}
    private static final class Entry {
        final Bucket bucket;
        volatile long lastAccess;
        Entry(Bucket bucket, long now) { this.bucket = bucket; this.lastAccess = now; }
    }
    private final Map<Key, Entry> cache = new ConcurrentHashMap<>();
    private final Map<Policy, Integer> limits;
    private final AtomicLong nextCleanup = new AtomicLong(System.nanoTime());
    private static final long IDLE_TTL = Duration.ofMinutes(10).toNanos();

    public RateLimitingService(
            @Value("${rate-limit.read-per-minute:360}") int read,
            @Value("${rate-limit.write-per-minute:120}") int write,
            @Value("${rate-limit.auth-per-minute:20}") int auth,
            @Value("${rate-limit.refresh-per-minute:60}") int refresh) {
        if (read < 1 || write < 1 || auth < 1 || refresh < 1) {
            throw new IllegalArgumentException("Os limites devem ser positivos");
        }
        limits = Map.of(Policy.READ, read, Policy.WRITE, write, Policy.AUTH, auth, Policy.REFRESH, refresh);
    }

    public int limit(Policy policy) { return limits.get(policy); }

    public Bucket resolverBucket(String ip, Policy policy) {
        long now = System.nanoTime();
        long cleanup = nextCleanup.get();
        if (now >= cleanup && nextCleanup.compareAndSet(cleanup, now + Duration.ofMinutes(1).toNanos())) {
            cache.entrySet().removeIf(entry -> now - entry.getValue().lastAccess > IDLE_TTL);
        }
        Entry entry = cache.computeIfAbsent(new Key(ip, policy), key -> new Entry(
                Bucket.builder().addLimit(Bandwidth.builder()
                        .capacity(limit(policy))
                        .refillGreedy(limit(policy), Duration.ofMinutes(1)).build()).build(), now));
        entry.lastAccess = now;
        return entry.bucket;
    }
}
