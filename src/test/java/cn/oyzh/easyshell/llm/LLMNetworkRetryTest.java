package cn.oyzh.easyshell.llm;

import dev.langchain4j.exception.LangChain4jException;
import org.junit.Test;

import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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

    @Test
    public void detectsClosedStreamErrors() {
        assertTrue(LLMNetworkRetry.isPrematureStreamClose(
                new LangChain4jException("closed")));
        assertTrue(LLMNetworkRetry.isPrematureStreamClose(
                new LangChain4jException(new IOException("closed"))));
        assertTrue(LLMNetworkRetry.isPrematureStreamClose(
                new LangChain4jException(new IOException("Stream closed"))));
        assertTrue(LLMNetworkRetry.isRetryable(
                new IllegalStateException("request failed",
                        new LangChain4jException("closed"))));
        assertTrue(LLMNetworkRetry.isPrematureStreamClose(new EOFException()));
    }

    @Test
    public void retriesClosedStream() throws Exception {
        AtomicInteger calls = new AtomicInteger();

        String result = LLMNetworkRetry.execute("test", () -> {
            if (calls.incrementAndGet() == 1) {
                throw new LangChain4jException("closed");
            }
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(2, calls.get());
    }

    @Test
    public void doesNotTreatCancellationAsClosedStream() {
        assertFalse(LLMNetworkRetry.isPrematureStreamClose(
                new InterruptedIOException("closed")));
        assertFalse(LLMNetworkRetry.isPrematureStreamClose(
                new CancellationException("closed")));
        assertFalse(LLMNetworkRetry.isPrematureStreamClose(
                new IllegalStateException("bad request")));
    }

}
