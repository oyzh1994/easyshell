package cn.oyzh.easyshell.llm;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.ModelProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.CompleteToolCall;
import dev.langchain4j.model.chat.response.PartialResponse;
import dev.langchain4j.model.chat.response.PartialResponseContext;
import dev.langchain4j.model.chat.response.PartialThinking;
import dev.langchain4j.model.chat.response.PartialThinkingContext;
import dev.langchain4j.model.chat.response.PartialToolCall;
import dev.langchain4j.model.chat.response.PartialToolCallContext;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.output.TokenUsage;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Captures token usage from every raw model call made during one user request.
 *
 * <p>Using raw model responses avoids double counting while still covering tool
 * loops, streaming and all internal Agent model calls.</p>
 *
 * @author oyzh
 * @since 2026-11-21
 */
public final class LLMTokenUsageTracker {

    private LLMTokenUsageStats stats = LLMTokenUsageStats.EMPTY;

    private int observations;

    private boolean terminalHandled;

    /**
     * Wraps a non-streaming model so every response usage is captured.
     *
     * @param delegate delegate model
     * @return tracking model
     */
    public ChatModel wrap(ChatModel delegate) {
        return new TrackingChatModel(delegate);
    }

    /**
     * Wraps a streaming model so the final usage of every stream is captured.
     *
     * @param delegate delegate model
     * @return tracking model
     */
    public StreamingChatModel wrap(StreamingChatModel delegate) {
        return new TrackingStreamingChatModel(delegate);
    }

    /**
     * Completes a successful request. The aggregate response usage is used only
     * when a provider did not expose usage through any raw response callback.
     *
     * @param fallbackUsage aggregate final usage
     * @param duration      request duration in milliseconds
     */
    public synchronized void complete(TokenUsage fallbackUsage, long duration) {
        if (this.terminalHandled) {
            return;
        }
        if (this.observations == 0) {
            this.stats = this.stats.record(fallbackUsage, duration);
        } else {
            this.stats = this.stats.withDuration(duration);
        }
        // A successful request without provider usage remains explicitly unknown.
        if (!this.stats.hasLastUsage()) {
            this.stats = this.stats.withoutLastUsage(duration);
        }
        this.terminalHandled = true;
    }

    /**
     * Marks the request as failed when no successful final response was received.
     *
     * @param duration request duration in milliseconds
     */
    public synchronized void fail(long duration) {
        if (this.terminalHandled) {
            return;
        }
        this.stats = this.stats.withoutLastUsage(duration);
        this.terminalHandled = true;
    }

    /**
     * Produces the final immutable statistics and treats unfinished requests as failed.
     *
     * @param duration request duration in milliseconds
     * @return final statistics
     */
    public synchronized LLMTokenUsageStats snapshot(long duration) {
        if (!this.terminalHandled) {
            this.fail(duration);
        }
        return this.stats;
    }

    private synchronized void record(TokenUsage usage) {
        this.observations++;
        this.stats = this.stats.record(usage, LLMTokenUsageStats.UNKNOWN);
    }

    private final class TrackingChatModel implements ChatModel {

        private final ChatModel delegate;

        private TrackingChatModel(ChatModel delegate) {
            this.delegate = delegate;
        }

        @Override
        public ChatResponse doChat(ChatRequest chatRequest) {
            ChatResponse response = this.delegate.chat(chatRequest);
            LLMTokenUsageTracker.this.record(response == null ? null : response.tokenUsage());
            return response;
        }

        @Override
        public CompletableFuture<ChatResponse> doChatAsync(ChatRequest chatRequest) {
            return this.delegate.chatAsync(chatRequest)
                    .thenApply(response -> {
                        LLMTokenUsageTracker.this.record(
                                response == null ? null : response.tokenUsage());
                        return response;
                    });
        }

        @Override
        public ChatRequestParameters defaultRequestParameters() {
            return this.delegate.defaultRequestParameters();
        }

        @Override
        public List<ChatModelListener> listeners() {
            // The delegate fires its own listeners; avoid duplicate notifications.
            return List.of();
        }

        @Override
        public ModelProvider provider() {
            return this.delegate.provider();
        }
    }

    private final class TrackingStreamingChatModel implements StreamingChatModel {

        private final StreamingChatModel delegate;

        private TrackingStreamingChatModel(StreamingChatModel delegate) {
            this.delegate = delegate;
        }

        @Override
        public void doChat(ChatRequest chatRequest, StreamingChatResponseHandler handler) {
            this.delegate.chat(chatRequest, new StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(String partialResponse) {
                    handler.onPartialResponse(partialResponse);
                }

                @Override
                public void onPartialResponse(PartialResponse partialResponse,
                                              PartialResponseContext context) {
                    handler.onPartialResponse(partialResponse, context);
                }

                @Override
                public void onPartialThinking(PartialThinking partialThinking) {
                    handler.onPartialThinking(partialThinking);
                }

                @Override
                public void onPartialThinking(PartialThinking partialThinking,
                                              PartialThinkingContext context) {
                    handler.onPartialThinking(partialThinking, context);
                }

                @Override
                public void onPartialToolCall(PartialToolCall partialToolCall) {
                    handler.onPartialToolCall(partialToolCall);
                }

                @Override
                public void onPartialToolCall(PartialToolCall partialToolCall,
                                              PartialToolCallContext context) {
                    handler.onPartialToolCall(partialToolCall, context);
                }

                @Override
                public void onCompleteToolCall(CompleteToolCall completeToolCall) {
                    handler.onCompleteToolCall(completeToolCall);
                }

                @Override
                public void onUnmappedRawEvent(Object rawEvent) {
                    handler.onUnmappedRawEvent(rawEvent);
                }

                @Override
                public void onCompleteResponse(ChatResponse completeResponse) {
                    LLMTokenUsageTracker.this.record(
                            completeResponse == null ? null : completeResponse.tokenUsage());
                    handler.onCompleteResponse(completeResponse);
                }

                @Override
                public void onError(Throwable error) {
                    handler.onError(error);
                }
            });
        }

        @Override
        public ChatRequestParameters defaultRequestParameters() {
            return this.delegate.defaultRequestParameters();
        }

        @Override
        public List<ChatModelListener> listeners() {
            return List.of();
        }

        @Override
        public ModelProvider provider() {
            return this.delegate.provider();
        }
    }
}
