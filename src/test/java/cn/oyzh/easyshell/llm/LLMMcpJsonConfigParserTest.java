package cn.oyzh.easyshell.llm;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LLMMcpJsonConfigParserTest {

    @Test
    public void parsesStandardMcpServersConfiguration() {
        List<LLMMcpServer> servers = LLMMcpJsonConfigParser.parse("""
                {
                  "mcpServers": {
                    "bing-search": {
                      "command": "npx",
                      "args": ["-y", "bing-cn-mcp"]
                    }
                  }
                }
                """);

        assertEquals(1, servers.size());
        LLMMcpServer server = servers.getFirst();
        assertEquals("bing-search", server.getName());
        assertEquals(LLMMcpTransport.STDIO, server.getTransport());
        assertTrue(server.isEnabled());
        assertEquals("npx -y bing-cn-mcp", server.getCommandText());
        assertEquals(List.of("npx", "-y", "bing-cn-mcp"),
                LLMMcpConfigParser.parseCommandLine(server.getCommandText()));
    }

    @Test
    public void parsesMultipleLocalAndRemoteServers() {
        List<LLMMcpServer> servers = LLMMcpJsonConfigParser.parse("""
                {
                  "mcpServers": {
                    "files": {
                      "command": "node",
                      "args": ["/tmp/my server.js"],
                      "env": {"TOKEN": "abc=def"},
                      "cwd": "/tmp",
                      "enabledTools": ["read_file", "search"]
                    },
                    "remote": {
                      "type": "streamable-http",
                      "url": "https://example.com/mcp",
                      "headers": {"Authorization": "Bearer token"}
                    },
                    "socket": {
                      "type": "websocket",
                      "url": "wss://example.com/mcp"
                    }
                  }
                }
                """);

        assertEquals(3, servers.size());
        LLMMcpServer files = servers.stream()
                .filter(server -> server.getName().equals("files")).findFirst().orElseThrow();
        assertEquals("/tmp", files.getWorkingDirectory());
        assertEquals("TOKEN=abc=def", files.getEnvironmentText());
        assertEquals("read_file, search", files.getToolFilterText());
        assertEquals(List.of("node", "/tmp/my server.js"),
                LLMMcpConfigParser.parseCommandLine(files.getCommandText()));

        LLMMcpServer remote = servers.stream()
                .filter(server -> server.getName().equals("remote")).findFirst().orElseThrow();
        assertEquals(LLMMcpTransport.STREAMABLE_HTTP, remote.getTransport());
        assertEquals("Authorization: Bearer token", remote.getHeadersText());

        LLMMcpServer socket = servers.stream()
                .filter(server -> server.getName().equals("socket")).findFirst().orElseThrow();
        assertEquals(LLMMcpTransport.WEBSOCKET, socket.getTransport());
    }

    @Test
    public void parsesDirectSingleServerAndDisabledFlag() {
        List<LLMMcpServer> servers = LLMMcpJsonConfigParser.parse("""
                {
                  "name": "direct",
                  "command": "npx",
                  "args": [],
                  "disabled": true
                }
                """);

        assertEquals(1, servers.size());
        assertEquals("direct", servers.getFirst().getName());
        assertFalse(servers.getFirst().isEnabled());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsUnsupportedSseTransport() {
        LLMMcpJsonConfigParser.parse("""
                {
                  "mcpServers": {
                    "legacy": {
                      "type": "sse",
                      "url": "http://localhost/mcp"
                    }
                  }
                }
                """);
    }
}
