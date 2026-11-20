package cn.oyzh.easyshell.llm.agent;

import cn.oyzh.easyshell.llm.LLMExecutionMode;
import cn.oyzh.easyshell.llm.tool.LLMTools;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.PartialThinking;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class LLMAgentRuntimeTest {

    @Test
    public void executesCompositePlanAndReportsProgress() throws Exception {
        LLMAgentRuntime runtime = runtime(new RoutingChatModel("""
                {"objective":"check and execute","steps":[
                  {"agent":"internal","objective":"collect application context"},
                  {"agent":"connection","objective":"inspect current database"}
                ]}
                """));
        List<LLMAgentEventType> events = new ArrayList<>();
        Map<String, LLMAgentTrace> traces = new LinkedHashMap<>();
        AtomicBoolean hasResponse = new AtomicBoolean();

        ChatResponse response = runtime.execute("inspect and verify", List.of(),
                new LLMAgentListener() {
                    @Override
                    public void onEvent(LLMAgentEvent event) {
                        events.add(event.type());
                    }

                    @Override
                    public void onPartialResponse(String text) {
                        if (!text.isBlank()) {
                            hasResponse.set(true);
                        }
                    }

                    @Override
                    public void onAgentTrace(LLMAgentTrace trace) {
                        traces.put(trace.id(), trace);
                    }
                }, () -> false);

        assertEquals("verified: inspect and verify", response.aiMessage().text());
        assertTrue(hasResponse.get());
        assertEquals(List.of(
                LLMAgentEventType.PLANNING,
                LLMAgentEventType.STEP_STARTED,
                LLMAgentEventType.STEP_COMPLETED,
                LLMAgentEventType.STEP_STARTED,
                LLMAgentEventType.STEP_COMPLETED,
                LLMAgentEventType.SYNTHESIZING,
                LLMAgentEventType.COMPLETED), events);
        assertEquals("planner thinking", traces.get("planner").thinking());
        assertEquals("worker thinking", traces.get("worker-0").thinking());
        assertEquals("worker: inspect and verify", traces.get("worker-0").conclusion());
        assertEquals("worker thinking", traces.get("worker-1").thinking());
        assertEquals("verifier thinking", traces.get("verifier").thinking());
        assertEquals("verified: inspect and verify", traces.get("verifier").conclusion());
    }

    @Test
    public void singleStepReturnsWorkerResultWithoutSynthesis() throws Exception {
        RoutingChatModel model = new RoutingChatModel("""
                {"objective":"query","steps":[
                  {"agent":"connection","objective":"query current connection"}
                ]}
                """);
        StringBuilder thinking = new StringBuilder();
        StringBuilder responseText = new StringBuilder();
        ChatResponse response = runtime(model).execute("query rows", List.of(),
                new LLMAgentListener() {
                    @Override
                    public void onPartialThinking(String text) {
                        thinking.append(text);
                    }

                    @Override
                    public void onPartialResponse(String text) {
                        responseText.append(text);
                    }
                }, () -> false);

        assertEquals("worker: query rows", response.aiMessage().text());
        assertEquals("worker thinking", thinking.toString());
        assertEquals("worker: query rows", responseText.toString());
        assertFalse(model.synthesisCalled);
    }

    @Test
    public void invalidPlanFallsBackToSingleConnectionAgent() throws Exception {
        RoutingChatModel model = new RoutingChatModel("not json");
        ChatResponse response = runtime(model).execute("simple task", List.of(),
                new LLMAgentListener() {
                }, () -> false);

        assertEquals("worker: simple task", response.aiMessage().text());
        assertEquals(1, model.workerCalls);
    }

    @Test
    public void compositeFinalResponseStreamsToListener() throws Exception {
        RoutingChatModel model = new RoutingChatModel("""
                {"objective":"check","steps":[
                  {"agent":"internal","objective":"collect context"},
                  {"agent":"connection","objective":"inspect connection"}
                ]}
                """);
        StringBuilder streamed = new StringBuilder();
        LLMAgentRuntime runtime = new LLMAgentRuntime(model,
                new StreamingModel(), List.of(), null,
                "connection tools", "internal tools", "mcp tools",
                "execution rules", "language rules", null);

        ChatResponse response = runtime.execute("stream task", List.of(),
                new LLMAgentListener() {
                    @Override
                    public void onPartialResponse(String text) {
                        streamed.append(text);
                    }
                }, () -> false);

        assertEquals("streamed", response.aiMessage().text());
        assertEquals("streamed", streamed.toString());
    }

    @Test
    public void streamingWorkerTraceContainsLiveThinkingAndConclusion() throws Exception {
        RoutingChatModel chatModel = new RoutingChatModel("""
                {"objective":"query","steps":[
                  {"agent":"connection","objective":"query current connection"}
                ]}
                """);
        Map<String, LLMAgentTrace> traces = new LinkedHashMap<>();
        LLMAgentRuntime runtime = new LLMAgentRuntime(chatModel,
                new StreamingWorkerModel(), List.of(), null,
                "connection tools", "internal tools", "mcp tools",
                "execution rules", "language rules", null);

        ChatResponse response = runtime.execute("query rows", List.of(),
                new LLMAgentListener() {
                    @Override
                    public void onAgentTrace(LLMAgentTrace trace) {
                        traces.put(trace.id(), trace);
                    }
                }, () -> false);

        assertEquals("streamed worker result", response.aiMessage().text());
        assertEquals("stream thinking", traces.get("worker-0").thinking());
        assertEquals("streamed worker result", traces.get("worker-0").conclusion());
    }

    @Test
    public void internalWorkerDoesNotReceiveConnectionTools() throws Exception {
        ToolAwareChatModel model = new ToolAwareChatModel("""
                {"objective":"inspect app","steps":[
                  {"agent":"internal","objective":"inspect application state"}
                ]}
                """, false);
        LLMAgentRuntime runtime = new LLMAgentRuntime(model, null,
                List.of(new EchoTools()), null,
                "connection tools", "internal tools", "mcp tools",
                "execution rules", "language rules", null);

        ChatResponse response = runtime.execute("inspect app", List.of(),
                new LLMAgentListener() {
                }, () -> false);

        assertEquals("worker: inspect app", response.aiMessage().text());
        assertEquals(2, model.requestTools.size());
        assertTrue(model.requestTools.stream().allMatch(List::isEmpty));
    }

    @Test
    public void connectionWorkerExecutesItsRegisteredTool() throws Exception {
        ToolAwareChatModel model = new ToolAwareChatModel("""
                {"objective":"echo","steps":[
                  {"agent":"connection","objective":"call the echo tool"}
                ]}
                """, true);
        EchoTools tools = new EchoTools();
        LLMAgentRuntime runtime = new LLMAgentRuntime(model, null,
                List.of(tools), null,
                "connection tools", "internal tools", "mcp tools",
                "execution rules", "language rules", null);

        ChatResponse response = runtime.execute("call echo", List.of(),
                new LLMAgentListener() {
                }, () -> false);

        assertEquals("tool result", response.aiMessage().text());
        assertEquals(1, tools.calls);
        assertTrue(model.requestTools.get(1).contains("echo_value"));
    }

    @Test
    public void streamingWorkerExecutesItsRegisteredTool() throws Exception {
        RoutingChatModel chatModel = new RoutingChatModel("""
                {"objective":"echo","steps":[
                  {"agent":"connection","objective":"call the echo tool"}
                ]}
                """);
        EchoTools tools = new EchoTools();
        Map<String, LLMAgentTrace> traces = new LinkedHashMap<>();
        LLMAgentRuntime runtime = new LLMAgentRuntime(chatModel,
                new StreamingToolChatModel(), List.of(tools), null,
                "connection tools", "internal tools", "mcp tools",
                "execution rules", "language rules", null);

        ChatResponse response = runtime.execute("call echo", List.of(),
                new LLMAgentListener() {
                    @Override
                    public void onAgentTrace(LLMAgentTrace trace) {
                        traces.put(trace.id(), trace);
                    }
                }, () -> false);

        assertEquals("streamed tool result", response.aiMessage().text());
        assertEquals(1, tools.calls);
        assertEquals("streamed tool result", traces.get("worker-0").conclusion());
    }

    @Test(timeout = 3000)
    public void streamingSynthesisStopsWaitingAfterCancellation() throws Exception {
        RoutingChatModel model = new RoutingChatModel("""
                {"objective":"check","steps":[
                  {"agent":"internal","objective":"collect context"},
                  {"agent":"connection","objective":"inspect connection"}
                ]}
                """);
        AtomicBoolean cancelled = new AtomicBoolean();
        LLMAgentRuntime runtime = new LLMAgentRuntime(model,
                new CancellingStreamingModel(cancelled), List.of(), null,
                "connection tools", "internal tools", "mcp tools",
                "execution rules", "language rules", null);

        try {
            runtime.execute("stream task", List.of(),
                    new LLMAgentListener() {
                    }, cancelled::get);
            fail("Cancellation should stop streaming synthesis");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("cancelled"));
        }
    }

    @Test
    public void cancellationAfterWorkerDoesNotStartSynthesis() throws Exception {
        AtomicBoolean cancelled = new AtomicBoolean();
        AtomicBoolean synthesisCalled = new AtomicBoolean();
        ChatModel model = new ChatModel() {
            @Override
            public ChatResponse doChat(ChatRequest request) {
                String prompt = request.messages().stream()
                        .filter(dev.langchain4j.data.message.UserMessage.class::isInstance)
                        .map(dev.langchain4j.data.message.UserMessage.class::cast)
                        .map(dev.langchain4j.data.message.UserMessage::singleText)
                        .findFirst()
                        .orElse("");
                if (prompt.contains("Create the minimum executable plan")) {
                    return RoutingChatModel.response("""
                            {"objective":"query","steps":[
                              {"agent":"connection","objective":"query current connection"}
                            ]}
                            """);
                }
                if (prompt.contains("Current step ")) {
                    cancelled.set(true);
                    return RoutingChatModel.response("worker result");
                }
                synthesisCalled.set(true);
                return RoutingChatModel.response("unexpected synthesis");
            }
        };

        try {
            runtime(model).execute("query rows", List.of(),
                    new LLMAgentListener() {
                    }, cancelled::get);
            fail("Cancellation should stop the composite agent");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("cancelled"));
        }
        assertFalse(synthesisCalled.get());
    }

    @Test
    public void listenerFailuresDoNotAbortTheRequest() throws Exception {
        RoutingChatModel model = new RoutingChatModel("""
                {"objective":"query","steps":[
                  {"agent":"connection","objective":"query current connection"}
                ]}
                """);
        ChatResponse response = runtime(model).execute("query rows", List.of(),
                new LLMAgentListener() {
                    @Override
                    public void onEvent(LLMAgentEvent event) {
                        throw new IllegalStateException("event listener failed");
                    }

                    @Override
                    public void onPartialResponse(String text) {
                        throw new IllegalStateException("response listener failed");
                    }
                }, () -> false);

        assertEquals("worker: query rows", response.aiMessage().text());
    }

    @Test
    public void traceFormatterIncludesPlannerWorkersAndVerifier() {
        LLMAgentTrace planner = LLMAgentTrace.running("planner",
                        LLMAgentTrace.Type.PLANNER, 0, 0, "supervisor", "task")
                .withThinking("planner thinking")
                .complete("plan");
        LLMAgentTrace worker = LLMAgentTrace.running("worker-0",
                        LLMAgentTrace.Type.WORKER, 0, 1, "connection", "query")
                .withThinking("worker thinking")
                .complete("worker conclusion");
        LLMAgentTrace verifier = LLMAgentTrace.running("verifier",
                        LLMAgentTrace.Type.VERIFIER, 0, 1, "verifier", "verify")
                .withThinking("verifier thinking")
                .complete("final answer");

        String trace = LLMAgentTraceFormatter.format(List.of(planner, worker, verifier));

        assertTrue(trace.contains("planner thinking"));
        assertTrue(trace.contains("worker thinking"));
        assertTrue(trace.contains("worker conclusion"));
        assertTrue(trace.contains("verifier thinking"));
        assertTrue(trace.contains("final answer"));
    }

    private static LLMAgentRuntime runtime(ChatModel model) {
        return new LLMAgentRuntime(model, null, List.of(), null,
                "connection tools", "internal tools", "mcp tools",
                "execution rules", "language rules", null);
    }

    private static final class RoutingChatModel implements ChatModel {

        private final String plan;

        private int workerCalls;

        private boolean synthesisCalled;

        private RoutingChatModel(String plan) {
            this.plan = plan;
        }

        @Override
        public ChatResponse doChat(ChatRequest request) {
            String prompt = request.messages().stream()
                    .filter(dev.langchain4j.data.message.UserMessage.class::isInstance)
                    .map(dev.langchain4j.data.message.UserMessage.class::cast)
                    .map(dev.langchain4j.data.message.UserMessage::singleText)
                    .findFirst()
                    .orElse("");
            if (prompt.contains("Create the minimum executable plan")) {
                return response(this.plan, "planner thinking");
            }
            if (prompt.contains("Current step ")) {
                this.workerCalls++;
                return response("worker: " + taskFromPrompt(prompt), "worker thinking");
            }
            if (prompt.contains("Worker results:")) {
                this.synthesisCalled = true;
                return response("verified: " + taskFromPrompt(prompt), "verifier thinking");
            }
            return response("unexpected");
        }

        private static String taskFromPrompt(String prompt) {
            String marker = "Original user task:\n";
            int start = prompt.indexOf(marker);
            if (start < 0) {
                return prompt;
            }
            start += marker.length();
            int end = prompt.indexOf('\n', start);
            return end < 0 ? prompt.substring(start) : prompt.substring(start, end);
        }

        private static ChatResponse response(String text) {
            return response(text, null);
        }

        private static ChatResponse response(String text, String thinking) {
            AiMessage.Builder message = AiMessage.builder().text(text);
            if (thinking != null) {
                message.thinking(thinking);
            }
            return ChatResponse.builder()
                    .aiMessage(message.build())
                    .build();
        }
    }

    private static final class StreamingModel implements StreamingChatModel {

        @Override
        public void doChat(ChatRequest request, StreamingChatResponseHandler handler) {
            handler.onPartialResponse("streamed");
            handler.onCompleteResponse(ChatResponse.builder()
                    .aiMessage(AiMessage.from("streamed"))
                    .build());
        }
    }

    private static final class StreamingWorkerModel implements StreamingChatModel {

        @Override
        public void doChat(ChatRequest request, StreamingChatResponseHandler handler) {
            handler.onPartialThinking(new PartialThinking("stream thinking"));
            handler.onCompleteResponse(ChatResponse.builder()
                    .aiMessage(AiMessage.builder()
                            .text("streamed worker result")
                            .thinking("stream thinking")
                            .build())
                    .build());
        }
    }

    private static final class StreamingToolChatModel implements StreamingChatModel {

        @Override
        public void doChat(ChatRequest request, StreamingChatResponseHandler handler) {
            boolean hasToolResult = request.messages().stream()
                    .anyMatch(ToolExecutionResultMessage.class::isInstance);
            if (!hasToolResult) {
                ToolExecutionRequest toolRequest = ToolExecutionRequest.builder()
                        .id("stream-echo-call")
                        .name("echo_value")
                        .arguments("{\"value\":\"hello\"}")
                        .build();
                handler.onCompleteResponse(ChatResponse.builder()
                        .aiMessage(AiMessage.from(toolRequest))
                        .build());
                return;
            }
            handler.onPartialResponse("streamed tool result");
            handler.onCompleteResponse(ChatResponse.builder()
                    .aiMessage(AiMessage.builder()
                            .text("streamed tool result")
                            .thinking("streamed tool thinking")
                            .build())
                    .build());
        }
    }

    private static final class CancellingStreamingModel implements StreamingChatModel {

        private final AtomicBoolean cancelled;

        private CancellingStreamingModel(AtomicBoolean cancelled) {
            this.cancelled = cancelled;
        }

        @Override
        public void doChat(ChatRequest request, StreamingChatResponseHandler handler) {
            this.cancelled.set(true);
        }
    }

    private static final class ToolAwareChatModel implements ChatModel {

        private final String plan;

        private final boolean executeTool;

        private final List<List<String>> requestTools = new ArrayList<>();

        private ToolAwareChatModel(String plan, boolean executeTool) {
            this.plan = plan;
            this.executeTool = executeTool;
        }

        @Override
        public ChatResponse doChat(ChatRequest request) {
            this.requestTools.add(request.toolSpecifications() == null
                    ? List.of()
                    : request.toolSpecifications().stream()
                    .map(specification -> specification.name())
                    .toList());
            String prompt = request.messages().stream()
                    .filter(dev.langchain4j.data.message.UserMessage.class::isInstance)
                    .map(dev.langchain4j.data.message.UserMessage.class::cast)
                    .map(dev.langchain4j.data.message.UserMessage::singleText)
                    .findFirst()
                    .orElse("");
            if (prompt.contains("Create the minimum executable plan")) {
                return RoutingChatModel.response(this.plan);
            }
            if (prompt.contains("Current step ")) {
                boolean hasToolResult = request.messages().stream()
                        .anyMatch(ToolExecutionResultMessage.class::isInstance);
                if (this.executeTool && !hasToolResult) {
                    ToolExecutionRequest toolRequest = ToolExecutionRequest.builder()
                            .id("echo-call")
                            .name("echo_value")
                            .arguments("{\"value\":\"hello\"}")
                            .build();
                    return ChatResponse.builder()
                            .aiMessage(AiMessage.from(toolRequest))
                            .build();
                }
                if (hasToolResult) {
                    return RoutingChatModel.response("tool result");
                }
                return RoutingChatModel.response(
                        "worker: " + RoutingChatModel.taskFromPrompt(prompt));
            }
            return RoutingChatModel.response("unexpected");
        }
    }

    private static final class EchoTools implements LLMTools {

        private int calls;

        @Tool(name = "echo_value", value = "Echo a value")
        public String echo(@P("value to echo") String value) {
            this.calls++;
            return "echo:" + value;
        }

        @Override
        public String systemMessage(LLMExecutionMode executionMode) {
            return "Echo tools";
        }
    }
}
