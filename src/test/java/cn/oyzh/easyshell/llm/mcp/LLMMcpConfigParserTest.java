package cn.oyzh.easyshell.llm.mcp;

import dev.langchain4j.agent.tool.ToolSpecification;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class LLMMcpConfigParserTest {

    @Test
    public void parsesQuotedCommandArguments() {
        List<String> command = LLMMcpConfigParser.parseCommandLine(
                "npx -y \"@modelcontextprotocol/server-filesystem\" '/tmp/my folder' --name=a\\ b");

        assertEquals(List.of(
                "npx",
                "-y",
                "@modelcontextprotocol/server-filesystem",
                "/tmp/my folder",
                "--name=a b"), command);
    }

    @Test
    public void parsesEnvironmentAndHeaders() {
        Map<String, String> environment = LLMMcpConfigParser.parseEnvironment("""
                TOKEN=abc=def
                # comment
                EMPTY=
                """);
        Map<String, String> headers = LLMMcpConfigParser.parseHeaders("""
                Authorization: Bearer ${MCP_TOKEN}
                X-Test: a:b
                """);

        assertEquals("abc=def", environment.get("TOKEN"));
        assertEquals("", environment.get("EMPTY"));
        assertEquals("Bearer ${MCP_TOKEN}", headers.get("Authorization"));
        assertEquals("a:b", headers.get("X-Test"));
    }

    @Test
    public void parsesToolAllowlist() {
        assertEquals(List.of("read_file", "write_file", "search"),
                LLMMcpConfigParser.parseToolNames("read_file\nwrite_file, search").stream().toList());
    }

    @Test
    public void generatedToolNamesAreValidAndStable() {
        LLMMcpServer first = new LLMMcpServer();
        first.setUid("server-1");
        first.setName("File Tools");
        LLMMcpServer second = new LLMMcpServer();
        second.setUid("server-2");
        second.setName("File Tools");

        String firstPrefix = LLMMcpToolSession.toolPrefix(first, first.getUid());
        String secondPrefix = LLMMcpToolSession.toolPrefix(second, second.getUid());
        String mapped = LLMMcpToolSession.mapToolName(firstPrefix, "read.file");
        String longName = LLMMcpToolSession.mapToolName(firstPrefix,
                "a_very_long_tool_name_that_would_otherwise_exceed_the_provider_limit_"
                        + "and_keep_going_for_a_while");

        assertNotEquals(firstPrefix, secondPrefix);
        assertTrue(mapped.matches("[a-z0-9_-]+"));
        assertTrue(longName.length() <= 64);
        assertEquals(mapped, LLMMcpToolSession.mapToolName(firstPrefix, "read.file"));
    }

    @Test
    public void disambiguatesToolNamesThatCollideAfterSanitization() {
        String prefix = LLMMcpToolSession.toolPrefix(server("collision"), "collision-key");
        Map<String, String> mappings = LLMMcpToolSession.resolveToolNameMappings(List.of(
                tool("read file"),
                tool("read_file"),
                tool("read-file")), prefix);

        assertEquals(3, mappings.size());
        assertEquals(3, Set.copyOf(mappings.values()).size());
        assertTrue(mappings.values().stream().allMatch(name -> name.length() <= 64));
        assertTrue(mappings.values().stream()
                .filter(name -> name.contains("read_file"))
                .count() >= 2);
    }

    @Test
    public void duplicateOriginalMcpToolNamesAreRejected() {
        String prefix = LLMMcpToolSession.toolPrefix(server("duplicate"), "duplicate-key");
        try {
            LLMMcpToolSession.resolveToolNameMappings(List.of(
                    tool("same_name"),
                    tool("same_name")), prefix);
            throw new AssertionError("Duplicate original MCP tool names must be rejected");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("same_name"));
        }
    }

    @Test
    public void invalidTransportOrdinalUsesStdioDefault() {
        LLMMcpServer server = new LLMMcpServer();
        server.setTransportType(999);

        assertEquals(LLMMcpTransport.STDIO.ordinal(), server.getTransportType());
        assertEquals(LLMMcpTransport.STDIO, server.getTransport());
    }

    private static LLMMcpServer server(String name) {
        LLMMcpServer server = new LLMMcpServer();
        server.setName(name);
        return server;
    }

    private static ToolSpecification tool(String name) {
        return ToolSpecification.builder()
                .name(name)
                .description(name)
                .build();
    }
}
