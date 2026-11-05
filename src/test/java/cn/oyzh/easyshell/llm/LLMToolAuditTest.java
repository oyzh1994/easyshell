package cn.oyzh.easyshell.llm;

import cn.oyzh.common.json.JSONUtil;
import com.alibaba.fastjson2.JSONObject;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.invocation.InvocationContext;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolExecution;
import dev.langchain4j.service.tool.ToolExecutionResult;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LLMToolAuditTest {

    @Test
    public void aiServicesHookRecordsArgumentsAndResult() throws Exception {
        Path file = Files.createTempFile("llm-tool-audit-", ".jsonl");
        RecordingTool tool = new RecordingTool();
        LLMToolAudit audit = new LLMToolAudit(file, List.of(tool), "session-1", "tab-1",
                "provider-1", "model-1", LLMExecutionMode.APPROVE);
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(new ForcedToolChatModel())
                .tools(tool)
                .afterToolExecution(audit::record)
                .build();

        assertEquals("done", assistant.chat("run"));

        JSONObject record = readSingleRecord(file);
        assertEquals("record", record.getString("toolName"));
        assertEquals(RecordingTool.class.getName(), record.getString("toolClass"));
        assertEquals("session-1", record.getString("sessionId"));
        assertEquals("tab-1", record.getString("sessionTabId"));
        assertEquals("provider-1", record.getString("provider"));
        assertEquals("model-1", record.getString("model"));
        assertEquals("APPROVE", record.getString("executionMode"));
        assertEquals("recorded: hello", record.getString("result"));
        assertFalse(record.getBooleanValue("failed"));
        assertTrue(record.getBooleanValue("argumentsRedacted"));
        assertFalse(record.getString("arguments").contains("secret-value"));
        assertTrue(record.getString("arguments").contains("***"));
    }

    @Test
    public void redactsNestedSensitiveArguments() throws Exception {
        Path file = Files.createTempFile("llm-tool-audit-", ".jsonl");
        LLMToolAudit audit = new LLMToolAudit(file, List.of(new RecordingTool()), null, null,
                null, null, null);
        String arguments = """
                {"name":"demo","password":"p1","nested":{"apiKey":"ak-1","private_key":"pk-1"},
                 "items":[{"token":"token-1"}],"authHeader":"Bearer abc","redisKey":"keep"}
                """;

        audit.record(execution("record", arguments, success("ok")));

        JSONObject record = readSingleRecord(file);
        JSONObject redacted = JSONUtil.parseObject(record.getString("arguments"));
        assertEquals("***", redacted.getString("password"));
        assertEquals("***", redacted.getJSONObject("nested").getString("apiKey"));
        assertEquals("***", redacted.getJSONObject("nested").getString("private_key"));
        assertEquals("***", redacted.getJSONArray("items").getJSONObject(0).getString("token"));
        assertEquals("***", redacted.getString("authHeader"));
        assertEquals("keep", redacted.getString("redisKey"));
        assertTrue(record.getBooleanValue("argumentsRedacted"));
    }

    @Test
    public void recordsFailedExecution() throws Exception {
        Path file = Files.createTempFile("llm-tool-audit-", ".jsonl");
        LLMToolAudit audit = new LLMToolAudit(file, List.of(new RecordingTool()), null, null,
                null, null, null);

        ToolExecutionResult result = ToolExecutionResult.builder()
                .isError(true)
                .resultText("permission denied")
                .build();
        audit.record(execution("record", "{\"value\":\"x\"}", result));

        JSONObject record = readSingleRecord(file);
        assertTrue(record.getBooleanValue("failed"));
        assertEquals("permission denied", record.getString("result"));
    }

    @Test
    public void redactsSensitiveTextWhenArgumentsAreNotJson() throws Exception {
        Path file = Files.createTempFile("llm-tool-audit-", ".jsonl");
        LLMToolAudit audit = new LLMToolAudit(file, List.of(new RecordingTool()), null, null,
                null, null, null);

        audit.record(execution("record", "password=secret-value token: token-value normal=1", success("ok")));

        JSONObject record = readSingleRecord(file);
        String arguments = record.getString("arguments");
        assertFalse(arguments.contains("secret-value"));
        assertFalse(arguments.contains("token-value"));
        assertTrue(arguments.contains("password=***"));
        assertTrue(arguments.contains("normal=1"));
        assertTrue(record.getBooleanValue("argumentsRedacted"));
    }

    @Test
    public void truncatesLongArgumentsAndResults() throws Exception {
        Path file = Files.createTempFile("llm-tool-audit-", ".jsonl");
        LLMToolAudit audit = new LLMToolAudit(file, List.of(new RecordingTool()), null, null,
                null, null, null);
        String longValue = "x".repeat(LLMToolAudit.MAX_TEXT_LENGTH + 100);
        String arguments = JSONUtil.toJson(java.util.Map.of("value", longValue));

        audit.record(execution("record", arguments, success(longValue)));

        JSONObject record = readSingleRecord(file);
        assertTrue(record.getBooleanValue("argumentsTruncated"));
        assertTrue(record.getBooleanValue("resultTruncated"));
        assertEquals(arguments.length(), record.getIntValue("argumentsLength"));
        assertEquals(longValue.length(), record.getIntValue("resultLength"));
        assertTrue(record.getString("arguments").endsWith("[truncated]"));
        assertTrue(record.getString("result").endsWith("[truncated]"));
    }

    private static JSONObject readSingleRecord(Path file) throws Exception {
        List<String> lines = Files.readAllLines(file);
        assertEquals(1, lines.size());
        return JSONUtil.parseObject(lines.getFirst());
    }

    private static ToolExecution execution(String name, String arguments, ToolExecutionResult result) {
        ToolExecutionRequest request = ToolExecutionRequest.builder()
                .id("call-1")
                .name(name)
                .arguments(arguments)
                .build();
        InvocationContext context = InvocationContext.builder()
                .interfaceName("TestAssistant")
                .methodName("chat")
                .chatMemoryId("context-session")
                .build();
        return ToolExecution.builder()
                .request(request)
                .result(result)
                .invocationContext(context)
                .build();
    }

    private static ToolExecutionResult success(String text) {
        return ToolExecutionResult.builder()
                .resultText(text)
                .build();
    }

    private interface Assistant {

        String chat(String message);
    }

    public static class RecordingTool {

        @Tool(name = "record", value = "Record the supplied value")
        public String record(@P(name = "value", value = "Value to record") String value,
                             @P(name = "password", value = "Optional password") String password) {
            return "recorded: " + value;
        }
    }

    private static class ForcedToolChatModel implements ChatModel {

        @Override
        public ChatResponse doChat(ChatRequest request) {
            if (request.messages().stream().anyMatch(ToolExecutionResultMessage.class::isInstance)) {
                return ChatResponse.builder()
                        .aiMessage(AiMessage.from("done"))
                        .build();
            }
            ToolExecutionRequest toolRequest = ToolExecutionRequest.builder()
                    .id("call-1")
                    .name("record")
                    .arguments("{\"value\":\"hello\",\"password\":\"secret-value\"}")
                    .build();
            return ChatResponse.builder()
                    .aiMessage(AiMessage.from(toolRequest))
                    .build();
        }
    }
}
