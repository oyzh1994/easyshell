package cn.oyzh.easyshell.llm.tool;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LLMToolNameStrategyTest {

    @Test
    public void suggestsSnakeCaseToolForSnakeCaseAlias() {
        assertEquals("execute_command",
                LLMToolNameStrategy.suggest(List.of("get_connection_info", "execute_command"), "execute_command"));
        assertEquals("list_files",
                LLMToolNameStrategy.suggest(List.of("list_file", "list_files"), "list_files"));
    }

    @Test
    public void returnsCorrectiveToolResultInsteadOfThrowing() {
        ToolExecutionRequest request = ToolExecutionRequest.builder()
                .id("call-1")
                .name("exec_command")
                .arguments("{\"command\":\"pwd\"}")
                .build();

        ToolExecutionResultMessage result = LLMToolNameStrategy.result(
                request, List.of("get_connection_info", "execute_command"));

        assertEquals("call-1", result.id());
        assertEquals("exec_command", result.toolName());
        assertEquals(Boolean.TRUE, result.isError());
        assertTrue(result.text().contains("Did you mean 'execute_command'?"));
    }

    @Test
    public void doesNotSuggestAnUnrelatedTool() {
        assertEquals(null,
                LLMToolNameStrategy.suggest(List.of("get_connection_info", "execute_command"), "delete_everything"));
    }
}
