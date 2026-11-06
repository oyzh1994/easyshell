package cn.oyzh.easyshell.llm;

import com.sun.net.httpserver.HttpServer;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LLMModelFetcherTest {

    @Test
    public void buildsOpenAiModelUrls() {
        assertEquals(List.of(
                        "https://api.deepseek.com/models",
                        "https://api.deepseek.com/v1/models"),
                LLMModelFetcher.buildUrls("https://api.deepseek.com/", LLMApiType.OPENAI));

        assertEquals(List.of("https://api.openai.com/v1/models"),
                LLMModelFetcher.buildUrls("https://api.openai.com/v1/chat/completions", LLMApiType.OPENAI));
    }

    @Test
    public void buildsProviderSpecificUrls() {
        assertEquals(List.of(
                        "https://api.anthropic.com/v1/models",
                        "https://api.anthropic.com/models"),
                LLMModelFetcher.buildUrls("https://api.anthropic.com", LLMApiType.ANTHROPIC));

        assertEquals(List.of("https://generativelanguage.googleapis.com/v1beta/models"),
                LLMModelFetcher.buildUrls("https://generativelanguage.googleapis.com", LLMApiType.GEMINI));

        assertEquals(List.of("http://localhost:11434/api/tags"),
                LLMModelFetcher.buildUrls("localhost:11434", LLMApiType.OLLAMA));

        assertEquals(List.of("https://example.com/v1beta/models"),
                LLMModelFetcher.buildUrls("example.com/v1beta", LLMApiType.GEMINI));
    }

    @Test
    public void buildsOpenAiFallbacksForAnthropicCompatibility() {
        assertEquals(List.of(
                        "https://api.deepseek.com/anthropic/v1/models",
                        "https://api.deepseek.com/models",
                        "https://api.deepseek.com/v1/models"),
                LLMModelFetcher.buildUrls("https://api.deepseek.com/anthropic/v1", LLMApiType.ANTHROPIC));

        assertEquals(List.of(
                        "https://token-plan-cn.xiaomimimo.com/anthropic/v1/models",
                        "https://token-plan-cn.xiaomimimo.com/models",
                        "https://token-plan-cn.xiaomimimo.com/v1/models"),
                LLMModelFetcher.buildUrls("https://token-plan-cn.xiaomimimo.com/anthropic/v1", LLMApiType.ANTHROPIC));
    }

    @Test
    public void parsesOpenAiAndAnthropicData() {
        assertEquals(List.of("gpt-4o", "gpt-4o-mini"),
                LLMModelFetcher.parseModels(
                        "{\"data\":[{\"id\":\"gpt-4o\"},{\"id\":\"gpt-4o-mini\"}]}",
                        LLMApiType.OPENAI));

        assertEquals(List.of("claude-sonnet-4"),
                LLMModelFetcher.parseModels(
                        "{\"data\":[{\"id\":\"claude-sonnet-4\",\"name\":\"Claude Sonnet 4\"}]}",
                        LLMApiType.ANTHROPIC));
    }

    @Test
    public void parsesGeminiAndOllamaModels() {
        assertEquals(List.of("gemini-2.0-flash", "gemini-2.0-pro"),
                LLMModelFetcher.parseModels(
                        "{\"models\":[{\"name\":\"models/gemini-2.0-flash\"},"
                                + "{\"name\":\"models/gemini-2.0-flash\"},"
                                + "{\"name\":\"models/gemini-2.0-pro\"}]}",
                        LLMApiType.GEMINI));

        assertEquals(List.of("llama3:latest", "qwen2.5"),
                LLMModelFetcher.parseModels(
                        "{\"models\":[{\"name\":\"llama3:latest\"},{\"model\":\"qwen2.5\"}]}",
                        LLMApiType.OLLAMA));
    }

    @Test
    public void handlesOtherShapesAndEmptyResponses() {
        assertEquals(List.of("model-a", "model-b"),
                LLMModelFetcher.parseModels("[\"model-a\",\"model-b\"]", LLMApiType.OPENAI));
        assertTrue(LLMModelFetcher.parseModels("", LLMApiType.OPENAI).isEmpty());
        assertTrue(LLMModelFetcher.parseModels("not-json", LLMApiType.OPENAI).isEmpty());
        assertTrue(LLMModelFetcher.parseModels("{\"data\":[]}", LLMApiType.OPENAI).isEmpty());
        assertTrue(LLMModelFetcher.buildUrls("", LLMApiType.OPENAI).isEmpty());
    }

    @Test
    public void fallsBackToRequestWithoutSk() throws Exception {
        AtomicInteger requestCount = new AtomicInteger();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/models", exchange -> {
            requestCount.incrementAndGet();
            if (exchange.getRequestHeaders().getFirst("Authorization") != null) {
                exchange.sendResponseHeaders(401, -1);
                exchange.close();
                return;
            }
            byte[] body = "{\"data\":[{\"id\":\"public-model\"}]}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        try {
            String baseUrl = "http://127.0.0.1:" + server.getAddress().getPort() + "/v1";
            assertEquals(List.of("public-model"), LLMModelFetcher.fetch(baseUrl, "invalid-sk", LLMApiType.OPENAI));
            assertEquals(2, requestCount.get());
        } finally {
            server.stop(0);
        }
    }
}
