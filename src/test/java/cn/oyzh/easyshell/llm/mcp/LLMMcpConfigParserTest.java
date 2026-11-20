package cn.oyzh.easyshell.llm.mcp;

import org.junit.Test;

import java.util.List;
import java.util.Map;

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
}
