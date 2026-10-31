package cn.oyzh.easyshell.llm;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Fetches the model list exposed by an LLM provider.
 *
 * @author oyzh
 * @since 2026-10-31
 */
public final class LLMModelFetcher {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(8);

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(20);

    private static final String[] ENDPOINT_SUFFIXES = {
            "/chat/completions", "/completions", "/responses", "/messages",
            "/api/chat", "/api/generate", "/api/tags", "/models"
    };

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private LLMModelFetcher() {
    }

    public static List<String> fetch(String baseUrl, String sk, LLMApiType apiType) throws Exception {
        if (apiType == null) {
            throw new IllegalArgumentException("LLM api type is required");
        }
        List<String> urls = buildUrls(baseUrl, apiType);
        if (urls.isEmpty()) {
            throw new IllegalArgumentException("LLM base url is required");
        }
        Exception lastException = null;
        for (String candidate : urls) {
            String url = candidate;
            if (apiType == LLMApiType.GEMINI && hasText(sk)) {
                url = appendQuery(url, "key", sk);
            }
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
                    .timeout(REQUEST_TIMEOUT)
                    .header("Accept", "application/json");
            if (hasText(sk)) {
                if (apiType == LLMApiType.ANTHROPIC) {
                    builder.header("x-api-key", sk).header("anthropic-version", "2023-06-01");
                } else if (apiType == LLMApiType.OPENAI) {
                    builder.header("Authorization", "Bearer " + sk);
                }
            }
            HttpResponse<String> response = HTTP_CLIENT.send(builder.GET().build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            int status = response.statusCode();
            if (status == 404 || status == 405) {
                lastException = new IllegalStateException("HTTP " + status);
                continue;
            }
            if (status < 200 || status >= 300) {
                throw new IllegalStateException(formatHttpError(status, response.body()));
            }
            return parseModels(response.body(), apiType);
        }
        throw lastException == null ? new IllegalStateException("No model endpoint available") : lastException;
    }

    /**
     * Build candidate model-list URLs for the supplied base URL.
     *
     * @param baseUrl provider base URL
     * @param apiType provider API type
     * @return candidate URLs
     */
    public static List<String> buildUrls(String baseUrl, LLMApiType apiType) {
        if (apiType == null || !hasText(baseUrl)) {
            return Collections.emptyList();
        }
        String base = normalizeBaseUrl(baseUrl, apiType);
        if (base.isEmpty()) {
            return Collections.emptyList();
        }
        base = stripEndpoint(base);
        if (apiType == LLMApiType.OPENAI) {
            if (endsWithPath(base, "/v1")) {
                return List.of(base + "/models");
            }
            return List.of(base + "/models", base + "/v1/models");
        }
        if (apiType == LLMApiType.ANTHROPIC) {
            return List.of(endsWithPath(base, "/v1") ? base + "/models" : base + "/v1/models");
        }
        if (apiType == LLMApiType.GEMINI) {
            if (endsWithPath(base, "/v1beta") || endsWithPath(base, "/v1")) {
                return List.of(base + "/models");
            }
            return List.of(base + "/v1beta/models");
        }
        return List.of(endsWithPath(base, "/api") ? base + "/tags" : base + "/api/tags");
    }

    /**
     * Parse model names from an OpenAI, Anthropic, Gemini or Ollama response.
     *
     * @param body    response body
     * @param apiType provider API type
     * @return model names
     */
    public static List<String> parseModels(String body, LLMApiType apiType) {
        if (!hasText(body)) {
            return Collections.emptyList();
        }
        Object root;
        try {
            root = JSON.parse(body);
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
        JSONArray array = null;
        if (root instanceof JSONArray jsonArray) {
            array = jsonArray;
        } else if (root instanceof JSONObject object) {
            array = object.getJSONArray("data");
            if (array == null) {
                array = object.getJSONArray("models");
            }
        }
        if (array == null || array.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> result = new LinkedHashSet<>();
        for (Object item : array) {
            String model = null;
            if (item instanceof JSONObject object) {
                model = firstText(object, "id", "name", "model");
            } else if (item != null) {
                model = item.toString();
            }
            if (!hasText(model)) {
                continue;
            }
            model = model.trim();
            if (apiType == LLMApiType.GEMINI && model.startsWith("models/")) {
                model = model.substring("models/".length());
            }
            if (!model.isEmpty()) {
                result.add(model);
            }
        }
        return List.copyOf(result);
    }

    private static String normalizeBaseUrl(String baseUrl, LLMApiType apiType) {
        String base = baseUrl.trim();
        int queryIndex = base.indexOf('?');
        if (queryIndex >= 0) {
            base = base.substring(0, queryIndex);
        }
        int fragmentIndex = base.indexOf('#');
        if (fragmentIndex >= 0) {
            base = base.substring(0, fragmentIndex);
        }
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        if (base.isEmpty()) {
            return base;
        }
        String lower = base.toLowerCase(Locale.ROOT);
        if (!lower.startsWith("http://") && !lower.startsWith("https://")) {
            boolean local = apiType == LLMApiType.OLLAMA
                    || lower.startsWith("localhost")
                    || lower.startsWith("127.")
                    || lower.startsWith("[::1]");
            base = (local ? "http://" : "https://") + base;
        }
        return base;
    }

    private static String stripEndpoint(String base) {
        String lower = base.toLowerCase(Locale.ROOT);
        for (String suffix : ENDPOINT_SUFFIXES) {
            if (lower.endsWith(suffix)) {
                base = base.substring(0, base.length() - suffix.length());
                while (base.endsWith("/")) {
                    base = base.substring(0, base.length() - 1);
                }
                return base;
            }
        }
        return base;
    }

    private static boolean endsWithPath(String path, String suffix) {
        return path.toLowerCase(Locale.ROOT).endsWith(suffix);
    }

    private static String firstText(JSONObject object, String... keys) {
        for (String key : keys) {
            String value = object.getString(key);
            if (hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private static String appendQuery(String url, String key, String value) {
        String separator = url.contains("?") ? "&" : "?";
        return url + separator + key + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String formatHttpError(int status, String body) {
        String message = parseErrorMessage(body);
        if (hasText(message)) {
            return "HTTP " + status + ": " + message;
        }
        return "HTTP " + status;
    }

    private static String parseErrorMessage(String body) {
        if (!hasText(body)) {
            return null;
        }
        try {
            Object root = JSON.parse(body);
            if (root instanceof JSONObject object) {
                Object error = object.get("error");
                if (error instanceof JSONObject errorObject) {
                    String message = errorObject.getString("message");
                    if (hasText(message)) {
                        return message;
                    }
                } else if (error != null) {
                    return error.toString();
                }
                String message = object.getString("message");
                if (hasText(message)) {
                    return message;
                }
            }
        } catch (Exception ignored) {
        }
        String message = body.trim();
        return message.length() > 300 ? message.substring(0, 300) + "..." : message;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
