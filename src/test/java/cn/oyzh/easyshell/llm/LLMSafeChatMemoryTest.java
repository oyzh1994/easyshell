package cn.oyzh.easyshell.llm;

import com.alibaba.fastjson2.JSON;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.ChatMemory;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class LLMSafeChatMemoryTest {

    @Test
    public void repairsUnescapedControlCharactersInToolArguments() {
        String malformed = "{\"command\":\"echo first\nsecond\"}";

        RecordingChatMemory delegate = new RecordingChatMemory();
        ChatMemory memory = LLMSafeChatMemory.wrap(delegate);
        memory.add(AiMessage.from(toolRequest(malformed)));

        String repaired = delegate.lastToolArguments();
        assertFalse(repaired.contains("\n"));
        assertEquals("echo first\nsecond", JSON.parseObject(repaired).getString("command"));
    }

    @Test
    public void replacesUnrecoverableToolArgumentsWithEmptyObject() {
        RecordingChatMemory delegate = new RecordingChatMemory();
        ChatMemory memory = LLMSafeChatMemory.wrap(delegate);
        memory.add(AiMessage.from(toolRequest("{\"command\"=\"echo\"}")));

        assertEquals("{}", delegate.lastToolArguments());
    }

    @Test
    public void leavesValidArgumentsAndRegularMessagesUnchanged() {
        String valid = "{\"command\":\"echo first\\nsecond\"}";
        RecordingChatMemory delegate = new RecordingChatMemory();
        ChatMemory memory = LLMSafeChatMemory.wrap(delegate);
        memory.add(AiMessage.from(toolRequest(valid)));
        memory.add(AiMessage.from("done"));

        assertEquals(valid, delegate.lastToolArguments());
        assertEquals("done", ((AiMessage) delegate.messages.get(1)).text());
    }

    private static ToolExecutionRequest toolRequest(String arguments) {
        return ToolExecutionRequest.builder()
                .id("call-1")
                .name("execCommand")
                .arguments(arguments)
                .build();
    }

    private static class RecordingChatMemory implements ChatMemory {

        private final List<ChatMessage> messages = new ArrayList<>();

        @Override
        public Object id() {
            return "test";
        }

        @Override
        public void add(ChatMessage message) {
            this.messages.add(message);
        }

        @Override
        public List<ChatMessage> messages() {
            return List.copyOf(this.messages);
        }

        @Override
        public void clear() {
            this.messages.clear();
        }

        private String lastToolArguments() {
            for (int index = this.messages.size() - 1; index >= 0; index--) {
                if (this.messages.get(index) instanceof AiMessage message
                        && message.hasToolExecutionRequests()) {
                    return message.toolExecutionRequests().getFirst().arguments();
                }
            }
            throw new AssertionError("No tool request found");
        }
    }
}
