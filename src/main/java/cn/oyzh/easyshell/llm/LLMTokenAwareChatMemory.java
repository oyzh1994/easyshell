package cn.oyzh.easyshell.llm;

import cn.oyzh.common.log.JulLog;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.service.memory.ChatMemoryService;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Message-count and token-budget aware chat memory.
 *
 * <p>The message window remains the hard storage bound. Token-aware trimming is
 * applied when messages are read for a model request, preserving the system
 * message, the latest user message and complete tool request/result groups.</p>
 *
 * @author oyzh
 * @since 2026-11-21
 */
public final class LLMTokenAwareChatMemory implements ChatMemory {

    private final Object id;

    private final int maxMessages;

    private final int maxTokens;

    private final List<ChatMessage> messages = new ArrayList<>();

    private volatile TrimReport lastTrim = TrimReport.EMPTY;

    private LLMTokenAwareChatMemory(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id");
        this.maxMessages = Math.max(1, builder.maxMessages);
        this.maxTokens = Math.max(1, builder.maxTokens);
    }

    @Override
    public Object id() {
        return this.id;
    }

    @Override
    public synchronized void add(ChatMessage message) {
        if (message == null) {
            return;
        }
        if (message instanceof SystemMessage systemMessage) {
            for (int index = this.messages.size() - 1; index >= 0; index--) {
                ChatMessage existing = this.messages.get(index);
                if (existing instanceof SystemMessage existingSystem) {
                    if (existingSystem.equals(systemMessage)) {
                        return;
                    }
                    this.messages.remove(index);
                }
            }
        }
        this.messages.add(message);
    }

    @Override
    public synchronized void set(Iterable<ChatMessage> messages) {
        this.messages.clear();
        if (messages != null) {
            for (ChatMessage message : messages) {
                this.add(message);
            }
        }
    }

    @Override
    public synchronized CompletableFuture<Void> addAsync(List<ChatMessage> messages) {
        if (messages != null) {
            messages.forEach(this::add);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public synchronized CompletableFuture<Void> setAsync(List<ChatMessage> messages) {
        this.set(messages);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public synchronized List<ChatMessage> messages() {
        return this.trim(List.copyOf(this.messages));
    }

    @Override
    public synchronized CompletableFuture<List<ChatMessage>> messagesAsync() {
        return CompletableFuture.completedFuture(this.trim(List.copyOf(this.messages)));
    }

    @Override
    public synchronized void clear() {
        this.messages.clear();
        this.lastTrim = TrimReport.EMPTY;
    }

    public int maxMessages() {
        return this.maxMessages;
    }

    public int maxTokens() {
        return this.maxTokens;
    }

    public TrimReport lastTrim() {
        return this.lastTrim;
    }

    public static Builder builder() {
        return new Builder();
    }

    private List<ChatMessage> trim(List<ChatMessage> source) {
        List<ChatMessage> messages = source == null ? List.of() : source;
        SystemMessage systemMessage = null;
        List<ChatMessage> body = new ArrayList<>(messages.size());
        for (ChatMessage message : messages) {
            if (message instanceof SystemMessage value && systemMessage == null) {
                systemMessage = value;
            } else if (!(message instanceof SystemMessage)) {
                body.add(message);
            }
        }

        List<MessageUnit> units = units(body);
        int latestUserUnit = latestUserUnit(units);
        int maxBodyMessages = Math.max(1,
                this.maxMessages - (systemMessage == null ? 0 : 1));
        long systemTokens = LLMTokenEstimator.estimate(systemMessage);
        long bodyBudget = Math.max(1, this.maxTokens - systemTokens);
        LinkedList<ChatMessage> retained = new LinkedList<>();
        long retainedTokens = 0;
        int retainedCount = 0;
        for (int i = units.size() - 1; i >= 0; i--) {
            MessageUnit unit = units.get(i);
            boolean required = i == latestUserUnit;
            boolean countFits = retainedCount + unit.messages.size() <= maxBodyMessages;
            boolean tokensFit = retainedTokens + unit.tokens <= bodyBudget;
            if (!required && (!countFits || !tokensFit)) {
                continue;
            }
            retained.addAll(0, unit.messages);
            retainedCount += unit.messages.size();
            retainedTokens += unit.tokens;
        }

        List<ChatMessage> result = new ArrayList<>(
                retained.size() + (systemMessage == null ? 0 : 1));
        if (systemMessage != null) {
            result.add(systemMessage);
        }
        result.addAll(retained);
        int omitted = Math.max(0, messages.size() - result.size());
        this.lastTrim = new TrimReport(
                omitted > 0,
                messages.size(), result.size(), omitted,
                systemTokens + retainedTokens, this.maxTokens,
                latestUserUnit >= 0);
        if (omitted > 0) {
            JulLog.info("llm token-aware memory trimmed: original={}, retained={}, "
                            + "estimatedTokens={}, budget={}, latestUserPreserved={}",
                    messages.size(), result.size(), systemTokens + retainedTokens,
                    this.maxTokens, latestUserUnit >= 0);
        }
        return List.copyOf(result);
    }

    private static List<MessageUnit> units(List<ChatMessage> messages) {
        List<MessageUnit> result = new ArrayList<>();
        for (int index = 0; index < messages.size(); index++) {
            ChatMessage message = messages.get(index);
            if (message instanceof ToolExecutionResultMessage) {
                if (!result.isEmpty() && result.getLast().isToolExchange()) {
                    MessageUnit unit = result.getLast();
                    unit.messages.add(message);
                    unit.tokens += LLMTokenEstimator.estimate(message);
                }
                // Orphan tool results are intentionally dropped.
                continue;
            }
            MessageUnit unit = new MessageUnit(message);
            result.add(unit);
            if (message instanceof AiMessage aiMessage && aiMessage.hasToolExecutionRequests()) {
                while (index + 1 < messages.size()
                        && messages.get(index + 1) instanceof ToolExecutionResultMessage) {
                    unit.messages.add(messages.get(++index));
                    unit.tokens += LLMTokenEstimator.estimate(
                            unit.messages.getLast());
                }
            }
        }
        return result;
    }

    private static int latestUserUnit(List<MessageUnit> units) {
        for (int i = units.size() - 1; i >= 0; i--) {
            if (units.get(i).containsUser()) {
                return i;
            }
        }
        return -1;
    }

    private static final class MessageUnit {

        private final List<ChatMessage> messages = new ArrayList<>();

        private long tokens;

        private MessageUnit(ChatMessage first) {
            this.messages.add(first);
            this.tokens = LLMTokenEstimator.estimate(first);
        }

        private boolean isToolExchange() {
            return !this.messages.isEmpty()
                    && this.messages.getFirst() instanceof AiMessage aiMessage
                    && aiMessage.hasToolExecutionRequests();
        }

        private boolean containsUser() {
            return this.messages.stream().anyMatch(UserMessage.class::isInstance);
        }
    }

    public static final class Builder {

        private Object id = ChatMemoryService.DEFAULT;

        private int maxMessages = 20;

        private int maxTokens = LLMParameterUtil.DEFAULT_HISTORY_TOKEN_BUDGET;

        public Builder id(Object id) {
            this.id = id;
            return this;
        }

        public Builder maxMessages(int maxMessages) {
            this.maxMessages = maxMessages;
            return this;
        }

        public Builder maxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public LLMTokenAwareChatMemory build() {
            return new LLMTokenAwareChatMemory(this);
        }
    }

    public record TrimReport(boolean trimmed,
                             int originalCount,
                             int retainedCount,
                             int omittedCount,
                             long estimatedTokens,
                             int tokenBudget,
                             boolean latestUserPreserved) {

        public static final TrimReport EMPTY = new TrimReport(
                false, 0, 0, 0, 0, 0, false);
    }
}
