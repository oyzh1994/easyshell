package cn.oyzh.easyshell.llm;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.network.NetworkUtil;

import java.io.EOFException;
import java.io.InterruptedIOException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

/**
 * Retries LLM transport calls interrupted by a reset connection or a stream
 * closed before the model completed its response.
 *
 * @author oyzh
 * @since 2026-11-21
 */
public final class LLMNetworkRetry {

    public static final int MAX_RETRIES = 3;

    private static final Pattern MESSAGE_SEPARATORS =
            Pattern.compile("[\\s\\p{Z}\\p{P}\\p{S}]+");

    private static final Set<String> STREAM_CLOSE_MESSAGES = Set.of(
            "closed",
            "streamclosed",
            "responseclosed",
            "connectionclosed",
            "socketclosed",
            "channelclosed",
            "sseclosed"
    );

    private static final List<String> STREAM_CLOSE_FRAGMENTS = List.of(
            "prematurelyclosed",
            "closedprematurely",
            "unexpectedlyclosed",
            "closedunexpectedly",
            "streamclosed",
            "responseclosed",
            "connectionclosed",
            "socketclosed",
            "channelclosed",
            "bodyclosed",
            "sseclosed"
    );

    private LLMNetworkRetry() {
    }

    public static <T> T execute(String operation, Callable<T> call) throws Exception {
        return execute(operation, call, null);
    }

    public static <T> T execute(String operation,
                                Callable<T> call,
                                BiConsumer<Integer, Throwable> onRetry) throws Exception {
        for (int retry = 0; ; retry++) {
            try {
                return call.call();
            } catch (Exception ex) {
                if (retry >= MAX_RETRIES || !isRetryable(ex)) {
                    throw ex;
                }
                int nextRetry = retry + 1;
                JulLog.warn("llm transport interrupted, retrying "
                        + nextRetry + "/" + MAX_RETRIES + ": " + operation, ex);
                if (onRetry != null) {
                    onRetry.accept(nextRetry, ex);
                }
                Thread.sleep(retryDelayMillis(nextRetry));
            }
        }
    }

    /**
     * Whether an LLM transport failure can be retried safely.
     *
     * @param throwable failure
     * @return {@code true} for connection resets and streams closed before completion
     */
    public static boolean isRetryable(Throwable throwable) {
        return NetworkUtil.isConnectionReset(throwable)
                || isPrematureStreamClose(throwable);
    }

    /**
     * Detects an SSE or HTTP response body closed before the model completed its response.
     * LangChain4j surfaces the JDK HTTP client's bare {@code IOException("closed")} as-is.
     *
     * @param throwable failure
     * @return detection result
     */
    public static boolean isPrematureStreamClose(Throwable throwable) {
        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        Throwable current = throwable;
        while (current != null && visited.add(current)) {
            if (current instanceof InterruptedException
                    || current instanceof InterruptedIOException
                    || current instanceof CancellationException) {
                return false;
            }
            if (current instanceof EOFException) {
                return true;
            }
            String message = current.getMessage();
            if (message != null) {
                String compact = MESSAGE_SEPARATORS.matcher(
                        message.toLowerCase(Locale.ROOT)).replaceAll("");
                if (STREAM_CLOSE_MESSAGES.contains(compact)
                        || STREAM_CLOSE_FRAGMENTS.stream().anyMatch(compact::contains)) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }

    public static long retryDelayMillis(int retry) {
        return Math.min(1_000L, 250L * (1L << Math.max(0, retry - 1)));
    }
}
