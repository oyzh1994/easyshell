package cn.oyzh.easyshell.llm;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.output.TokenUsage;
import org.junit.Test;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class LLMTokenUsageTrackerTest {

    @Test
    public void tracksEveryRawChatCallWithoutDoubleCountingAggregateResponse() {
        LLMTokenUsageTracker tracker = new LLMTokenUsageTracker();
        ChatModel model = tracker.wrap(new UsageChatModel(
                new TokenUsage(100, 20, 120),
                new TokenUsage(150, 30, 180)));
        ChatRequest request = ChatRequest.builder()
                .messages(UserMessage.from("hello"))
                .build();

        model.chat(request);
        model.chat(request);
        tracker.complete(new TokenUsage(250, 50, 300), 4_200);
        LLMTokenUsageStats stats = tracker.snapshot(4_200);

        assertTrue(stats.cumulativeUsageComplete());
        assertEquals(150, stats.contextTokens());
        assertEquals(250, stats.sessionInputTokens());
        assertEquals(50, stats.sessionOutputTokens());
        assertEquals(300, stats.sessionTotalTokens());
        assertEquals(4_200, stats.lastDurationMillis());
    }

    @Test
    public void capturesStreamingCompleteResponseUsage() {
        LLMTokenUsageTracker tracker = new LLMTokenUsageTracker();
        StreamingChatModel model = tracker.wrap(new UsageStreamingModel(
                new TokenUsage(320, 80, 400)));
        AtomicReference<ChatResponse> completed = new AtomicReference<>();

        model.chat(ChatRequest.builder()
                        .messages(UserMessage.from("stream"))
                        .build(),
                new StreamingChatResponseHandler() {
                    @Override
                    public void onCompleteResponse(ChatResponse completeResponse) {
                        completed.set(completeResponse);
                    }

                    @Override
                    public void onError(Throwable error) {
                    }
                });
        tracker.complete(null, 3_000);
        LLMTokenUsageStats stats = tracker.snapshot(3_000);

        assertNotNull(completed.get());
        assertEquals(320, stats.contextTokens());
        assertEquals(400, stats.sessionTotalTokens());
        assertEquals(3_000, stats.lastDurationMillis());
    }

    @Test
    public void successfulProviderWithoutUsageRemainsExplicitlyUnknown() {
        LLMTokenUsageTracker tracker = new LLMTokenUsageTracker();
        ChatModel model = tracker.wrap(new UsageChatModel());

        model.chat(ChatRequest.builder()
                .messages(UserMessage.from("hello"))
                .build());
        tracker.complete(null, 500);
        LLMTokenUsageStats stats = tracker.snapshot(500);

        assertFalse(stats.hasLastUsage());
        assertFalse(stats.cumulativeUsageComplete());
        assertEquals(LLMTokenUsageStats.UNKNOWN, stats.contextTokens());
        assertEquals(500, stats.lastDurationMillis());
    }

    private static final class UsageChatModel implements ChatModel {

        private final Queue<TokenUsage> usages = new ArrayDeque<>();

        private UsageChatModel(TokenUsage... usages) {
            for (TokenUsage usage : usages) {
                if (usage != null) {
                    this.usages.add(usage);
                }
            }
        }

        @Override
        public ChatResponse doChat(ChatRequest chatRequest) {
            return ChatResponse.builder()
                    .aiMessage(AiMessage.from("ok"))
                    .tokenUsage(this.usages.poll())
                    .build();
        }
    }

    private static final class UsageStreamingModel implements StreamingChatModel {

        private final TokenUsage usage;

        private UsageStreamingModel(TokenUsage usage) {
            this.usage = usage;
        }

        @Override
        public void doChat(ChatRequest chatRequest, StreamingChatResponseHandler handler) {
            handler.onCompleteResponse(ChatResponse.builder()
                    .aiMessage(AiMessage.from("ok"))
                    .tokenUsage(this.usage)
                    .build());
        }
    }
}
