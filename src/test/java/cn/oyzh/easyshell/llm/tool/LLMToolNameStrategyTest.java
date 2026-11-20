package cn.oyzh.easyshell.llm.tool;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class LLMToolNameStrategyTest {

    @Test
    public void suggestsSnakeCaseToolForSnakeCaseAlias() {
        assertEquals("ssh_execute_command",
                LLMToolNameStrategy.suggest(List.of("ssh_get_connection_info", "ssh_execute_command"),
                        "ssh_execute_command"));
        assertEquals("list_files",
                LLMToolNameStrategy.suggest(List.of("list_file", "list_files"), "list_files"));
    }

    @Test
    public void returnsCorrectiveToolResultInsteadOfThrowing() {
        ToolExecutionRequest request = ToolExecutionRequest.builder()
                .id("call-1")
                .name("ssh_exec_command")
                .arguments("{\"command\":\"pwd\"}")
                .build();

        ToolExecutionResultMessage result = LLMToolNameStrategy.result(
                request, List.of("ssh_get_connection_info", "ssh_execute_command"));

        assertEquals("call-1", result.id());
        assertEquals("ssh_exec_command", result.toolName());
        assertEquals(Boolean.TRUE, result.isError());
        assertTrue(result.text().contains("Did you mean 'ssh_execute_command'?"));
    }

    @Test
    public void doesNotSuggestAnUnrelatedTool() {
        assertEquals(null,
                LLMToolNameStrategy.suggest(
                        List.of("ssh_get_connection_info", "ssh_execute_command"), "delete_everything"));
    }

    @Test
    public void rejectsDuplicateToolNamesBeforeRegistration() {
        try {
            LLMToolNameStrategy.validateUnique(
                    List.of(new FirstToolGroup(), new SecondToolGroup()), List.of());
            fail("Duplicate tool names must be rejected");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("duplicate_tool"));
            assertTrue(ex.getMessage().contains(FirstToolGroup.class.getName()));
            assertTrue(ex.getMessage().contains(SecondToolGroup.class.getName()));
        }
    }

    @Test
    public void acceptsUniqueToolNames() {
        LLMToolNameStrategy.validateUnique(
                List.of(new FirstToolGroup()), List.of("mcp_server__search"));
    }

    private static final class FirstToolGroup {

        @Tool(name = "duplicate_tool", value = "first")
        public String first() {
            return "first";
        }
    }

    private static final class SecondToolGroup {

        @Tool(name = "duplicate_tool", value = "second")
        public String second() {
            return "second";
        }
    }
}
