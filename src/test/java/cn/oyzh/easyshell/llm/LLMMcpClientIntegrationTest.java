package cn.oyzh.easyshell.llm;

import com.alibaba.fastjson2.JSON;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolExecutionResult;
import org.junit.Assume;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LLMMcpClientIntegrationTest {

    @Test
    public void initializesListsAndExecutesToolsOverStdio() throws Exception {
        LLMMcpServer server = fixtureServer();

        try (McpClient client = LLMMcpClientFactory.createClient(server)) {
            List<ToolSpecification> tools = client.listTools();
            assertTrue(tools.stream().anyMatch(tool -> tool.name().equals("echo")));

            ToolExecutionResult result = client.executeTool(ToolExecutionRequest.builder()
                    .id("call-1")
                    .name("echo")
                    .arguments("{\"text\":\"hello\"}")
                    .build());
            assertTrue(!result.isError());
            assertEquals("echo:hello", result.resultText());
        }
    }

    @Test
    public void exposesMcpToolsThroughAiServices() throws Exception {
        LLMMcpServer server = fixtureServer();

        try (LLMMcpToolSession session = LLMMcpToolSession.connect(List.of(server))) {
            assertTrue(session.hasClients());
            String echoTool = session.getToolNames().stream()
                    .filter(name -> !name.startsWith("mcp_resources_"))
                    .findFirst()
                    .orElseThrow();
            Assistant assistant = AiServices.builder(Assistant.class)
                    .chatModel(new ForcedMcpToolModel(echoTool))
                    .toolProvider(session.getToolProvider())
                    .build();

            assertEquals("done", assistant.chat("run"));
        }
    }

    @Test
    public void isolatesInvalidServerFromAvailableServers() throws Exception {
        LLMMcpServer invalid = new LLMMcpServer();
        invalid.setName("invalid");
        invalid.setTransport(LLMMcpTransport.STREAMABLE_HTTP);
        invalid.setUrl("ftp://127.0.0.1/mcp");

        try (LLMMcpToolSession session = LLMMcpToolSession.connect(
                List.of(invalid, fixtureServer()))) {
            assertTrue(session.hasClients());
            assertEquals(1, session.getErrors().size());
            assertTrue(session.getToolNames().stream().anyMatch(name -> name.contains("echo")));
        }
    }

    @Test
    public void connectsUsingImportedStandardJsonConfiguration() throws Exception {
        Path script = Path.of("src/test/resources/llm/mcp-test-server.py").toAbsolutePath();
        Assume.assumeTrue(Files.isRegularFile(script));
        Assume.assumeTrue(commandAvailable("python3"));
        String config = """
                {
                  "mcpServers": {
                    "fixture": {
                      "command": "python3",
                      "args": [%s]
                    }
                  }
                }
                """.formatted(JSON.toJSONString(script.toString()));
        LLMMcpServer server = LLMMcpJsonConfigParser.parse(config).getFirst();

        try (McpClient client = LLMMcpClientFactory.createClient(server)) {
            assertTrue(client.listTools().stream().anyMatch(tool -> tool.name().equals("echo")));
        }
    }

    @Test
    public void reportsConnectionProgressForAllServers() {
        LLMMcpServer first = fixtureServer();
        LLMMcpServer second = fixtureServer();
        second.setName("fixture-2");
        CopyOnWriteArrayList<LLMMcpToolSession.Progress> progress = new CopyOnWriteArrayList<>();

        try (LLMMcpToolSession session = LLMMcpToolSession.connect(
                List.of(first, second), progress::add)) {
            assertTrue(session.hasClients());
            assertEquals(3, progress.size());
            assertEquals(0, progress.getFirst().completed());
            assertEquals(2, progress.getFirst().total());
            assertEquals(2, progress.getLast().completed());
            assertEquals(2, progress.getLast().totalTools());
            assertTrue(progress.getLast().error() == null);
        }
    }

    private static LLMMcpServer fixtureServer() {
        Path script = Path.of("src/test/resources/llm/mcp-test-server.py").toAbsolutePath();
        Assume.assumeTrue(Files.isRegularFile(script));
        Assume.assumeTrue(commandAvailable("python3"));
        LLMMcpServer server = new LLMMcpServer();
        server.setName("fixture");
        server.setTransport(LLMMcpTransport.STDIO);
        server.setCommandText("python3 \"" + script + "\"");
        server.setTimeoutSeconds(10);
        return server;
    }

    private static boolean commandAvailable(String command) {
        try {
            Process process = new ProcessBuilder(command, "--version")
                    .redirectErrorStream(true)
                    .start();
            process.getInputStream().transferTo(java.io.OutputStream.nullOutputStream());
            return process.waitFor() == 0;
        } catch (Exception ex) {
            return false;
        }
    }

    private interface Assistant {

        String chat(String message);
    }

    private record ForcedMcpToolModel(String toolName) implements ChatModel {

        @Override
        public ChatResponse doChat(ChatRequest request) {
            if (request.messages().stream().anyMatch(ToolExecutionResultMessage.class::isInstance)) {
                return ChatResponse.builder()
                        .aiMessage(AiMessage.from("done"))
                        .build();
            }
            ToolExecutionRequest toolRequest = ToolExecutionRequest.builder()
                    .id("call-1")
                    .name(this.toolName)
                    .arguments("{\"text\":\"hello\"}")
                    .build();
            return ChatResponse.builder()
                    .aiMessage(AiMessage.from(toolRequest))
                    .build();
        }
    }
}
