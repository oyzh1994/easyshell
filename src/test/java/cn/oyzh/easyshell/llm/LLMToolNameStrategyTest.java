package cn.oyzh.easyshell.llm;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LLMToolNameStrategyTest {

    @Test
    public void suggestsCamelCaseToolForSnakeCaseAlias() {
        assertEquals("execCommand",
                LLMToolNameStrategy.suggest(List.of("getConnectionInfo", "execCommand"), "exec_command"));
        assertEquals("executeCommand",
                LLMToolNameStrategy.suggest(List.of("execCommand", "executeCommand"), "execute_command"));
    }

    @Test
    public void returnsCorrectiveToolResultInsteadOfThrowing() {
        ToolExecutionRequest request = ToolExecutionRequest.builder()
                .id("call-1")
                .name("exec_command")
                .arguments("{\"command\":\"pwd\"}")
                .build();

        ToolExecutionResultMessage result = LLMToolNameStrategy.result(
                request, List.of("getConnectionInfo", "execCommand"));

        assertEquals("call-1", result.id());
        assertEquals("exec_command", result.toolName());
        assertEquals(Boolean.TRUE, result.isError());
        assertTrue(result.text().contains("Did you mean 'execCommand'?"));
    }

    @Test
    public void doesNotSuggestAnUnrelatedTool() {
        assertEquals(null,
                LLMToolNameStrategy.suggest(List.of("getConnectionInfo", "execCommand"), "delete_everything"));
    }
}
