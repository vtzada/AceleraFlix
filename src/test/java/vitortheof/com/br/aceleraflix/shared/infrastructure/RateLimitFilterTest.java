package vitortheof.com.br.aceleraflix.shared.infrastructure;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;
import static vitortheof.com.br.aceleraflix.shared.infrastructure.RateLimitingService.Policy;

class RateLimitFilterTest {
    private MockHttpServletRequest request(String method, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setServletPath(path);
        request.setRemoteAddr("127.0.0.1");
        return request;
    }
    private MockHttpServletResponse send(RateLimitFilter filter, MockHttpServletRequest request) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> res.getWriter().write("ok"));
        return response;
    }

    @Test void normalBrowsingHasRoomForMoreThanSixtyRequests() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(new RateLimitingService(360, 120, 20, 60), false);
        for (int i = 0; i < 100; i++) assertEquals(200, send(filter, request("GET", "/videos")).getStatus());
    }
    @Test void loginExhaustionDoesNotBlockBrowsingWritesOrRefresh() throws Exception {
        RateLimitingService service = new RateLimitingService(3, 3, 1, 3);
        RateLimitFilter filter = new RateLimitFilter(service, false);
        assertEquals(200, send(filter, request("POST", "/auth/login")).getStatus());
        MockHttpServletResponse denied = send(filter, request("POST", "/auth/register"));
        assertEquals(429, denied.getStatus());
        assertNotNull(denied.getHeader("Retry-After"));
        assertTrue(denied.getContentAsString().contains("\"mensagem\""));
        assertEquals(200, send(filter, request("GET", "/videos")).getStatus());
        assertEquals(200, send(filter, request("POST", "/videos")).getStatus());
        assertEquals(200, send(filter, request("POST", "/auth/refresh")).getStatus());
    }
    @Test void preflightAndHealthDoNotConsumeNavigationAllowance() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(new RateLimitingService(1, 1, 1, 1), false);
        for (int i = 0; i < 5; i++) {
            assertEquals(200, send(filter, request("OPTIONS", "/videos")).getStatus());
            assertEquals(200, send(filter, request("GET", "/actuator/health")).getStatus());
        }
        assertEquals(200, send(filter, request("GET", "/videos")).getStatus());
        assertEquals(429, send(filter, request("GET", "/videos")).getStatus());
    }
    @Test void arbitraryForwardedHeadersCannotResetTheLimitByDefault() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(new RateLimitingService(1, 1, 1, 1), false);
        MockHttpServletRequest first = request("POST", "/auth/login");
        first.addHeader("X-Forwarded-For", "192.0.2.1");
        MockHttpServletRequest second = request("POST", "/auth/login");
        second.addHeader("X-Forwarded-For", "192.0.2.2");
        assertEquals(200, send(filter, first).getStatus());
        assertEquals(429, send(filter, second).getStatus());
    }
    @Test void tokensRefillContinuouslyInsteadOfWaitingForTheNextMinute() {
        RateLimitingService service = new RateLimitingService(360, 120, 20, 60);
        var bucket = service.resolverBucket("127.0.0.1", Policy.READ);
        assertTrue(bucket.tryConsume(360));
        var probe = bucket.tryConsumeAndReturnRemaining(1);
        assertFalse(probe.isConsumed());
        assertTrue(probe.getNanosToWaitForRefill() <= 167_000_000L);
    }
}
