package cn.oyzh.easyshell.llm;

import cn.oyzh.common.thread.TaskManager;

import java.util.List;
import java.util.function.Consumer;

/**
 * Provider model-list fetch action shared by add and update dialogs.
 *
 * @author oyzh
 * @since 2026-10-31
 */
public final class LLMProviderFetchUtil {

    private LLMProviderFetchUtil() {
    }

    /**
     * Fetch models asynchronously.
     * <p>
     * All callbacks are invoked on the JavaFX application thread. This method
     * intentionally has no dependency on concrete UI controls so that callers
     * can decide how the result should be displayed.
     *
     * @param baseUrl    provider API address
     * @param apiKey     provider API key
     * @param apiType    provider API type
     * @param onSuccess  called with the fetched model list
     * @param onFailure  called when fetching fails
     * @param onComplete called after either callback finishes
     */
    public static void fetch(String baseUrl,
                             String apiKey,
                             LLMApiType apiType,
                             Consumer<List<String>> onSuccess,
                             Consumer<Exception> onFailure,
                             Runnable onComplete) {
        TaskManager.startAsync(() -> {
            try {
                List<String> models = LLMModelFetcher.fetch(baseUrl, apiKey, apiType);
                onSuccess.accept(models);
            } catch (Exception ex) {
                onFailure.accept(ex);
            } finally {
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
    }

}
