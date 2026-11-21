package cn.oyzh.easyshell.llm;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LLMTokenAwareChatMemoryTest {

    @Test
    public void trimsOldHistoryButPreservesLatestUserAndToolExchange() {
        ToolExecutionRequest request = ToolExecutionRequest.builder()
                .id("call-1")
                .name("exec")
                .arguments("{\"command\":\"date\"}")
                .build();
        AiMessage toolRequest = AiMessage.from(request);
        ToolExecutionResultMessage toolResult =
                new ToolExecutionResultMessage("call-1", "exec", "r".repeat(500));
        ChatMessage latestUser = UserMessage.from("latest request");
        LLMTokenAwareChatMemory memory = LLMTokenAwareChatMemory.builder()
                .maxMessages(20)
                .maxTokens(1_500)
                .build();
        memory.set(List.of(
                SystemMessage.from("system instructions"),
                UserMessage.from("o".repeat(4_000)),
                AiMessage.from("a".repeat(4_000)),
                latestUser,
                toolRequest,
                toolResult,
                AiMessage.from("final answer")));

        List<ChatMessage> messages = memory.messages();

        assertTrue(messages.getFirst() instanceof SystemMessage);
        assertTrue(messages.contains(latestUser));
        int requestIndex = messages.indexOf(toolRequest);
        assertTrue(requestIndex >= 0);
        assertEquals(toolResult, messages.get(requestIndex + 1));
        assertFalse(messages.stream()
                .anyMatch(message -> message instanceof UserMessage user
                        && user.singleText().startsWith("ooo")));
        assertTrue(memory.lastTrim().trimmed());
        assertTrue(memory.lastTrim().latestUserPreserved());
    }

    @Test
    public void enforcesMessageCountAndDropsOrphanToolResults() {
        LLMTokenAwareChatMemory memory = LLMTokenAwareChatMemory.builder()
                .maxMessages(2)
                .maxTokens(100_000)
                .build();
        memory.set(List.of(
                SystemMessage.from("system"),
                UserMessage.from("old question"),
                new ToolExecutionResultMessage("orphan", "exec", "orphan result"),
                UserMessage.from("latest question")));

        List<ChatMessage> messages = memory.messages();

        assertTrue(messages.getFirst() instanceof SystemMessage);
        assertTrue(messages.stream().anyMatch(message ->
                message instanceof UserMessage user
                        && "latest question".equals(user.singleText())));
        assertFalse(messages.stream().anyMatch(ToolExecutionResultMessage.class::isInstance));
        assertTrue(messages.size() <= 2);
    }

    @Test
    public void reservesContextSpaceForPromptsToolsAndOutput() {
        assertEquals(50_791,
                LLMParameterUtil.historyTokenBudget(64 * 1024, 8 * 1024));
        assertEquals(209_715,
                LLMParameterUtil.historyTokenBudget(256 * 1024, 8 * 1024));
    }
}
