#!/usr/bin/env python3
import json
import os
import sys

SUPPORTS_RESOURCES = os.getenv("EASY_SHELL_MCP_FIXTURE_RESOURCES", "1") != "0"


def respond(message_id, result=None, error=None):
    response = {"jsonrpc": "2.0", "id": message_id}
    if error is not None:
        response["error"] = error
    else:
        response["result"] = result
    print(json.dumps(response, separators=(",", ":")), flush=True)


for line in sys.stdin:
    line = line.strip()
    if not line:
        continue
    request = json.loads(line)
    message_id = request.get("id")
    method = request.get("method")
    if message_id is None:
        continue
    if method == "initialize":
        params = request.get("params") or {}
        capabilities = {"tools": {"listChanged": False}}
        if SUPPORTS_RESOURCES:
            capabilities["resources"] = {}
        respond(message_id, {
            "protocolVersion": params.get("protocolVersion", "2025-06-18"),
            "capabilities": capabilities,
            "serverInfo": {"name": "easy-shell-mcp-fixture", "version": "1.0"},
            "instructions": "Use echo to return text."
        })
    elif method == "tools/list":
        respond(message_id, {
            "tools": [{
                "name": "echo",
                "description": "Echo text",
                "inputSchema": {
                    "type": "object",
                    "properties": {"text": {"type": "string"}},
                    "required": ["text"]
                }
            }]
        })
    elif method == "tools/call":
        params = request.get("params") or {}
        arguments = params.get("arguments") or {}
        text = arguments.get("text", "")
        respond(message_id, {
            "content": [{"type": "text", "text": "echo:" + str(text)}],
            "isError": False
        })
    elif SUPPORTS_RESOURCES and method in ("resources/list", "resources/templates/list", "prompts/list"):
        key = "resources" if method.startswith("resources/") else "prompts"
        if method == "resources/templates/list":
            key = "resourceTemplates"
        respond(message_id, {key: []})
    elif method == "ping":
        respond(message_id, {})
    else:
        respond(message_id, error={"code": -32601, "message": "Method not found"})
