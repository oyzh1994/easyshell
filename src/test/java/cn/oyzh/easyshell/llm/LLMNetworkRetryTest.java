package cn.oyzh.easyshell.llm;

import org.junit.Test;

import java.net.SocketException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class LLMNetworkRetryTest {

    @Test
    public void retriesConnectionResetAtMostThreeTimes() throws Exception {
        AtomicInteger calls = new AtomicInteger();

        String result = LLMNetworkRetry.execute("test", () -> {
            int call = calls.incrementAndGet();
            if (call <= 3) {
                throw new SocketException("Connection reset");
            }
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(4, calls.get());
    }

    @Test
    public void stopsAfterThirdRetry() {
        AtomicInteger calls = new AtomicInteger();
        try {
            LLMNetworkRetry.execute("test", () -> {
                calls.incrementAndGet();
                throw new SocketException("Connection reset");
            });
            fail("Connection reset should be rethrown after three retries");
        } catch (Exception ex) {
            assertEquals("Connection reset", ex.getMessage());
        }
        assertEquals(4, calls.get());
    }

    @Test
    public void doesNotRetryUnrelatedFailure() {
        AtomicInteger calls = new AtomicInteger();
        try {
            LLMNetworkRetry.execute("test", () -> {
                calls.incrementAndGet();
                throw new IllegalStateException("bad request");
            });
            fail("Unrelated failures must not be retried");
        } catch (Exception ex) {
            assertEquals("bad request", ex.getMessage());
        }
        assertEquals(1, calls.get());
    }

}
