package cn.oyzh.easyshell.llm;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.network.NetworkUtil;

import java.util.concurrent.Callable;
import java.util.function.BiConsumer;

/**
 * Retries LLM transport calls interrupted by a disconnected or reset connection.
 *
 * @author oyzh
 * @since 2026-11-21
 */
public final class LLMNetworkRetry {

    public static final int MAX_RETRIES = 3;

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
                if (retry >= MAX_RETRIES || !NetworkUtil.isConnectionReset(ex)) {
                    throw ex;
                }
                int nextRetry = retry + 1;
                JulLog.warn("llm connection reset, retrying {}/{}: {}",
                        nextRetry, MAX_RETRIES, operation);
                if (onRetry != null) {
                    onRetry.accept(nextRetry, ex);
                }
                Thread.sleep(retryDelayMillis(nextRetry));
            }
        }
    }

    public static long retryDelayMillis(int retry) {
        return Math.min(1_000L, 250L * (1L << Math.max(0, retry - 1)));
    }
}
