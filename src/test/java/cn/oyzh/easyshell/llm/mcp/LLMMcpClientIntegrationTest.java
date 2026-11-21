package cn.oyzh.easyshell.llm.mcp;

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
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LLMMcpClientIntegrationTest {

    @AfterClass
    public static void closeConnectionCache() {
        LLMMcpToolSession.closeAllConnections();
    }

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
            assertTrue(session.getToolNames().contains(LLMMcpToolSession.RESOURCE_LIST_TOOL));
            assertTrue(session.getToolNames().contains(LLMMcpToolSession.RESOURCE_GET_TOOL));
            assertTrue(session.getSystemMessage().contains("Resource helpers"));
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
    public void omitsResourceHelpersWhenNoServerSupportsResources() throws Exception {
        LLMMcpServer server = fixtureServer(false);

        try (LLMMcpToolSession session = LLMMcpToolSession.connect(List.of(server))) {
            assertTrue(session.hasClients());
            assertTrue(session.getToolNames().stream().anyMatch(name -> name.contains("echo")));
            assertTrue(!session.getToolNames().contains(LLMMcpToolSession.RESOURCE_LIST_TOOL));
            assertTrue(!session.getToolNames().contains(LLMMcpToolSession.RESOURCE_GET_TOOL));
            assertTrue(session.getSystemMessage().contains("Resources: unavailable"));
            assertTrue(session.getSystemMessage().contains("do not replace ordinary MCP tools"));
        }
    }

    @Test
    public void resourceHelpersUseOnlyResourceCapableServers() throws Exception {
        LLMMcpServer resources = fixtureServer();
        resources.setName("resources");
        LLMMcpServer toolsOnly = fixtureServer(false);
        toolsOnly.setName("tools-only");
        AtomicReference<String> resourceResult = new AtomicReference<>();

        try (LLMMcpToolSession session = LLMMcpToolSession.connect(
                List.of(toolsOnly, resources))) {
            assertTrue(session.getToolNames().contains(LLMMcpToolSession.RESOURCE_LIST_TOOL));
            assertTrue(session.getSystemMessage().contains("Resource-capable servers: resources"));
            Assistant assistant = AiServices.builder(Assistant.class)
                    .chatModel(new ResourceListModel(resourceResult))
                    .toolProvider(session.getToolProvider())
                    .build();

            assertEquals("done", assistant.chat("list resources"));
            assertEquals("[]", resourceResult.get());
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

    @Test
    public void reusesConnectionAcrossShortLivedSessions() throws Exception {
        LLMMcpServer server = fixtureServer();
        LLMMcpConnectionManager manager = LLMMcpConnectionManager.getInstance();
        manager.closeAll();
        LLMMcpToolSession first = null;
        LLMMcpToolSession second = null;
        try {
            first = LLMMcpToolSession.connect(List.of(server));
            assertTrue(first.hasClients());
            assertEquals(1, manager.cacheSize());

            long hits = manager.cacheHits();
            second = LLMMcpToolSession.connect(List.of(server));
            assertTrue(second.hasClients());
            assertEquals(1, manager.cacheSize());
            assertTrue(manager.cacheHits() > hits);

            String echoTool = second.getToolNames().stream()
                    .filter(name -> !name.startsWith("mcp_resources_"))
                    .findFirst()
                    .orElseThrow();
            Assistant assistant = AiServices.builder(Assistant.class)
                    .chatModel(new ForcedMcpToolModel(echoTool))
                    .toolProvider(second.getToolProvider())
                    .build();

            first.close();
            first = null;
            assertEquals("done", assistant.chat("run while another request released the lease"));
            assertEquals(1, manager.cacheSize());
        } finally {
            if (first != null) {
                first.close();
            }
            if (second != null) {
                second.close();
            }
            manager.closeAll();
        }
    }

    @Test
    public void refreshesConnectionWhenServerConfigurationChanges() throws Exception {
        LLMMcpServer server = fixtureServer();
        LLMMcpConnectionManager manager = LLMMcpConnectionManager.getInstance();
        manager.closeAll();
        LLMMcpToolSession first = null;
        LLMMcpToolSession second = null;
        try {
            first = LLMMcpToolSession.connect(List.of(server));
            assertTrue(first.getToolNames().contains(LLMMcpToolSession.RESOURCE_LIST_TOOL));

            server.setEnvironmentText("EASY_SHELL_MCP_FIXTURE_RESOURCES=0");
            second = LLMMcpToolSession.connect(List.of(server));
            assertTrue(!second.getToolNames().contains(LLMMcpToolSession.RESOURCE_LIST_TOOL));
            assertEquals(1, manager.cacheSize());
        } finally {
            if (first != null) {
                first.close();
            }
            if (second != null) {
                second.close();
            }
            manager.closeAll();
        }
    }

    @Test
    public void evictsCachedConnectionWhenServersAreDisabled() throws Exception {
        LLMMcpConnectionManager manager = LLMMcpConnectionManager.getInstance();
        manager.closeAll();
        LLMMcpToolSession session = null;
        LLMMcpToolSession empty = null;
        try {
            session = LLMMcpToolSession.connect(List.of(fixtureServer()));
            assertTrue(session.hasClients());
            assertEquals(1, manager.cacheSize());
            session.close();
            session = null;

            empty = LLMMcpToolSession.connect(List.of());
            assertTrue(!empty.hasClients());
            assertEquals(0, manager.cacheSize());
        } finally {
            if (session != null) {
                session.close();
            }
            if (empty != null) {
                empty.close();
            }
            manager.closeAll();
        }
    }

    private static LLMMcpServer fixtureServer() {
        return fixtureServer(true);
    }

    private static LLMMcpServer fixtureServer(boolean resources) {
        Path script = Path.of("src/test/resources/llm/mcp-test-server.py").toAbsolutePath();
        Assume.assumeTrue(Files.isRegularFile(script));
        Assume.assumeTrue(commandAvailable("python3"));
        LLMMcpServer server = new LLMMcpServer();
        server.setName("fixture");
        server.setTransport(LLMMcpTransport.STDIO);
        server.setCommandText("python3 \"" + script + "\"");
        server.setTimeoutSeconds(10);
        if (!resources) {
            server.setEnvironmentText("EASY_SHELL_MCP_FIXTURE_RESOURCES=0");
        }
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

    private record ResourceListModel(AtomicReference<String> result) implements ChatModel {

        @Override
        public ChatResponse doChat(ChatRequest request) {
            List<ToolExecutionResultMessage> results = request.messages().stream()
                    .filter(ToolExecutionResultMessage.class::isInstance)
                    .map(ToolExecutionResultMessage.class::cast)
                    .toList();
            if (!results.isEmpty()) {
                this.result.set(results.getLast().text());
                return ChatResponse.builder()
                        .aiMessage(AiMessage.from("done"))
                        .build();
            }
            ToolExecutionRequest toolRequest = ToolExecutionRequest.builder()
                    .id("resource-list-1")
                    .name(LLMMcpToolSession.RESOURCE_LIST_TOOL)
                    .arguments("{}")
                    .build();
            return ChatResponse.builder()
                    .aiMessage(AiMessage.from(toolRequest))
                    .build();
        }
    }
}
