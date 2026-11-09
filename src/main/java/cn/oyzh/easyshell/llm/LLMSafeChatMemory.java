package cn.oyzh.easyshell.llm;

import cn.oyzh.common.log.JulLog;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONValidator;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.ChatMemory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Sanitizes tool-call arguments before they are replayed in a later LLM request.
 *
 * <p>Some models occasionally emit tool arguments containing unescaped control characters.
 * LangChain4j keeps those raw arguments so the tool error can be returned to the model,
 * but Anthropic-compatible providers reject the malformed value when it is replayed as
 * {@code tool_use.input}. This wrapper stores a valid equivalent in chat memory while the
 * current tool invocation continues to use the original request and its error result.</p>
 *
 * @author oyzh
 * @since 2026-11-09
 */
public final class LLMSafeChatMemory implements ChatMemory {

    private final ChatMemory delegate;

    private LLMSafeChatMemory(ChatMemory delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    /**
     * Wraps a chat memory so historical tool arguments are always valid JSON.
     *
     * @param memory chat memory
     * @return wrapped chat memory
     */
    public static ChatMemory wrap(ChatMemory memory) {
        Objects.requireNonNull(memory, "memory");
        return memory instanceof LLMSafeChatMemory ? memory : new LLMSafeChatMemory(memory);
    }

    @Override
    public Object id() {
        return this.delegate.id();
    }

    @Override
    public void add(ChatMessage message) {
        this.delegate.add(sanitize(message));
    }

    @Override
    public void set(Iterable<ChatMessage> messages) {
        this.delegate.set(sanitize(messages));
    }

    @Override
    public List<ChatMessage> messages() {
        return this.delegate.messages();
    }

    @Override
    public CompletableFuture<Void> addAsync(List<ChatMessage> messages) {
        return this.delegate.addAsync(sanitize(messages));
    }

    @Override
    public CompletableFuture<Void> setAsync(List<ChatMessage> messages) {
        return this.delegate.setAsync(sanitize(messages));
    }

    @Override
    public CompletableFuture<List<ChatMessage>> messagesAsync() {
        return this.delegate.messagesAsync();
    }

    @Override
    public void clear() {
        this.delegate.clear();
    }

    static ChatMessage sanitize(ChatMessage message) {
        if (!(message instanceof AiMessage aiMessage) || !aiMessage.hasToolExecutionRequests()) {
            return message;
        }
        List<ToolExecutionRequest> requests = aiMessage.toolExecutionRequests();
        List<ToolExecutionRequest> sanitized = new ArrayList<>(requests.size());
        boolean changed = false;
        for (ToolExecutionRequest request : requests) {
            String arguments = sanitizeArguments(request);
            changed |= !Objects.equals(arguments, request.arguments());
            sanitized.add(request.toBuilder().arguments(arguments).build());
        }
        return changed
                ? aiMessage.toBuilder().toolExecutionRequests(List.copyOf(sanitized)).build()
                : message;
    }

    private static List<ChatMessage> sanitize(Iterable<ChatMessage> messages) {
        List<ChatMessage> sanitized = new ArrayList<>();
        messages.forEach(message -> sanitized.add(sanitize(message)));
        return sanitized;
    }

    private static List<ChatMessage> sanitize(List<ChatMessage> messages) {
        return messages.stream().map(LLMSafeChatMemory::sanitize).toList();
    }

    private static String sanitizeArguments(ToolExecutionRequest request) {
        String arguments = request.arguments();
        if (arguments == null || arguments.isBlank()) {
            return "{}";
        }
        if (!containsControlCharacter(arguments) && JSONValidator.from(arguments).validate()) {
            return arguments;
        }
        try {
            String sanitized = JSON.parseObject(arguments).toJSONString();
            JulLog.warn("llm tool arguments repaired in chat memory, tool: {}, id: {}",
                    request.name(), request.id());
            return sanitized;
        } catch (Exception ex) {
            JulLog.warn("llm tool arguments replaced in chat memory, tool: {}, id: {}",
                    request.name(), request.id());
            return "{}";
        }
    }

    private static boolean containsControlCharacter(String value) {
        for (int index = 0; index < value.length(); index++) {
            if (value.charAt(index) < 0x20) {
                return true;
            }
        }
        return false;
    }
}
