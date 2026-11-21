package cn.oyzh.easyshell.llm.agent;

import cn.oyzh.easyshell.llm.LLMExecutionMode;
import cn.oyzh.easyshell.llm.session.LLMSessionContext;
import cn.oyzh.easyshell.llm.tool.LLMToolScope;
import cn.oyzh.easyshell.llm.tool.LLMTools;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.PartialThinking;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import org.junit.Test;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.CopyOnWriteArrayList;

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
                "session tools", "internal tools", "local execution tools", "mcp tools",
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
                "session tools", "internal tools", "local execution tools", "mcp tools",
                "execution rules", "language rules", null);
        AtomicReference<String> runningConclusion = new AtomicReference<>("");

        ChatResponse response = runtime.execute("query rows", List.of(),
                new LLMAgentListener() {
                    @Override
                    public void onAgentTrace(LLMAgentTrace trace) {
                        traces.put(trace.id(), trace);
                        if ("worker-0".equals(trace.id())
                                && trace.status() == LLMAgentTrace.Status.RUNNING) {
                            runningConclusion.set(trace.conclusion());
                        }
                    }
                }, () -> false);

        assertEquals("streamed worker result", response.aiMessage().text());
        assertEquals("stream thinking", traces.get("worker-0").thinking());
        assertEquals("streamed worker result", traces.get("worker-0").conclusion());
        assertEquals("streamed worker result", runningConclusion.get());
    }

    @Test
    public void internalWorkerInNoSessionModeReceivesInternalTools() throws Exception {
        ToolAwareChatModel model = new ToolAwareChatModel("""
                {"objective":"inspect app","steps":[
                  {"agent":"internal","objective":"inspect application state"}
                ]}
                """, false);
        EchoTools tools = new EchoTools(LLMToolScope.INTERNAL);
        LLMAgentRuntime runtime = new LLMAgentRuntime(model, null,
                List.of(tools), null,
                "session tools", "internal tools", "local execution tools", "mcp tools",
                "execution rules", "language rules", null);

        ChatResponse response = runtime.execute("inspect app", List.of(),
                new LLMAgentListener() {
                }, () -> false);

        assertEquals("worker: inspect app", response.aiMessage().text());
        assertEquals(2, model.requestTools.size());
        assertTrue(model.requestTools.get(0).isEmpty());
        assertTrue(model.requestTools.get(1).contains("echo_value"));
    }

    @Test
    public void sessionWorkerReceivesOnlySessionTools() throws Exception {
        ToolAwareChatModel model = new ToolAwareChatModel("""
                {"objective":"inspect session","steps":[
                  {"agent":"connection","objective":"inspect current session"}
                ]}
                """, false);
        SessionEchoTools sessionTools = new SessionEchoTools();
        LocalEchoTools localTools = new LocalEchoTools();
        LLMAgentRuntime runtime = new LLMAgentRuntime(model, null,
                List.of(sessionTools, localTools), null,
                "session tools", "internal tools", "local execution tools", "mcp tools",
                "execution rules", "language rules", null);

        runtime.execute("inspect session", List.of(), new LLMAgentListener() {
        }, () -> false);

        List<String> workerTools = model.requestTools.get(1);
        assertTrue(workerTools.contains("session_echo"));
        assertFalse(workerTools.contains("local_echo"));
        assertFalse(workerTools.contains("echo_value"));
    }

    @Test
    public void completeModeRoutesInternalWorkerWithoutSessionTools() throws Exception {
        ToolAwareChatModel model = new ToolAwareChatModel("""
                {"objective":"inspect app","steps":[
                  {"agent":"internal","objective":"inspect application state"}
                ]}
                """, false);
        SessionEchoTools sessionTools = new SessionEchoTools();
        EchoTools internalTools = new EchoTools(LLMToolScope.INTERNAL);
        LocalEchoTools localTools = new LocalEchoTools();
        LLMAgentRuntime runtime = new LLMAgentRuntime(model, null,
                List.of(sessionTools, localTools, internalTools), null,
                "session tools", "internal tools", "local execution tools", "mcp tools",
                "execution rules", "language rules", null);

        runtime.execute("inspect app", List.of(), new LLMAgentListener() {
        }, () -> false);

        List<String> workerTools = model.requestTools.get(1);
        assertTrue(workerTools.contains("echo_value"));
        assertFalse(workerTools.contains("session_echo"));
        assertFalse(workerTools.contains("local_echo"));
    }

    @Test
    public void staleInternalPlanIsCorrectedToSessionScope() throws Exception {
        ToolAwareChatModel model = new ToolAwareChatModel("""
                {"objective":"inspect session","steps":[
                  {"agent":"internal","objective":"inspect current session"}
                ]}
                """, false);
        SessionEchoTools sessionTools = new SessionEchoTools();
        LocalEchoTools localTools = new LocalEchoTools();
        LLMAgentRuntime runtime = new LLMAgentRuntime(model, null,
                List.of(sessionTools, localTools), null,
                "session tools", "internal tools", "local execution tools", "mcp tools",
                "execution rules", "language rules", null);

        runtime.execute("inspect session", List.of(), new LLMAgentListener() {
        }, () -> false);

        List<String> workerTools = model.requestTools.get(1);
        assertTrue(workerTools.contains("session_echo"));
        assertFalse(workerTools.contains("local_echo"));
        assertFalse(workerTools.contains("echo_value"));
    }

    @Test
    public void internalWorkerWithoutSessionReceivesOnlyInternalTools() throws Exception {
        ToolAwareChatModel model = new ToolAwareChatModel("""
                {"objective":"inspect app","steps":[
                  {"agent":"internal","objective":"inspect application state"}
                ]}
                """, false);
        EchoTools internalTools = new EchoTools(LLMToolScope.INTERNAL);
        LocalEchoTools localTools = new LocalEchoTools();
        LLMAgentRuntime runtime = new LLMAgentRuntime(model, null,
                List.of(internalTools, localTools), null,
                "session tools", "internal tools", "local execution tools", "mcp tools",
                "execution rules", "language rules", null);

        runtime.execute("inspect app", List.of(), new LLMAgentListener() {
        }, () -> false);

        List<String> workerTools = model.requestTools.get(1);
        assertTrue(workerTools.contains("echo_value"));
        assertFalse(workerTools.contains("local_echo"));
    }

    @Test
    public void localWorkerReceivesOnlyLocalExecutionTools() throws Exception {
        ToolAwareChatModel model = new ToolAwareChatModel("""
                {"objective":"inspect local machine","steps":[
                  {"agent":"local","objective":"inspect the machine running EasyShell"}
                ]}
                """, false);
        SessionEchoTools sessionTools = new SessionEchoTools();
        EchoTools internalTools = new EchoTools(LLMToolScope.INTERNAL);
        LocalEchoTools localTools = new LocalEchoTools();
        LLMAgentRuntime runtime = new LLMAgentRuntime(model, null,
                List.of(sessionTools, internalTools, localTools), null,
                "session tools", "internal tools", "local execution tools", "mcp tools",
                "execution rules", "language rules", null);

        runtime.execute("inspect local machine", List.of(), new LLMAgentListener() {
        }, () -> false);

        List<String> workerTools = model.requestTools.get(1);
        assertTrue(workerTools.contains("local_echo"));
        assertFalse(workerTools.contains("session_echo"));
        assertFalse(workerTools.contains("echo_value"));
    }

    @Test
    public void plannerCatalogSeparatesSessionInternalLocalAndMcp() throws Exception {
        ToolAwareChatModel model = new ToolAwareChatModel("""
                {"objective":"inspect session","steps":[
                  {"agent":"connection","objective":"inspect current session"}
                ]}
                """, false);
        SessionEchoTools sessionTools = new SessionEchoTools();
        EchoTools internalTools = new EchoTools(LLMToolScope.INTERNAL);
        LocalEchoTools localTools = new LocalEchoTools();
        LLMAgentRuntime runtime = new LLMAgentRuntime(model, null,
                List.of(sessionTools, internalTools, localTools), null,
                "session tools", "internal tools", "local execution tools", "mcp tools",
                "execution rules", "language rules", null);

        runtime.execute("inspect session", List.of(), new LLMAgentListener() {
        }, () -> false);

        String plannerRequest = model.requestTexts.getFirst();
        assertTrue(plannerRequest.contains("connection: session_echo"));
        assertTrue(plannerRequest.contains("internal: echo_value"));
        assertTrue(plannerRequest.contains("local: local_echo"));
        assertTrue(plannerRequest.contains("mcp available: no"));
        assertTrue(plannerRequest.contains("Never use one scope as a fallback"));
    }

    @Test
    public void plannerAndWorkerReceiveTheSameBoundSessionContext() throws Exception {
        ToolAwareChatModel model = new ToolAwareChatModel("""
                {"objective":"inspect session","steps":[
                  {"agent":"connection","objective":"inspect current session"}
                ]}
                """, false);
        LLMSessionContext context = new LLMSessionContext(
                "tab-1", "server-a", "ssh", "10.0.0.12", 22, "root");
        LLMAgentRuntime runtime = new LLMAgentRuntime(model, null,
                List.of(new SessionEchoTools()), null,
                "session tools", "internal tools", "local execution tools",
                context, "mcp tools", "execution rules", "language rules", null);

        runtime.execute("inspect session", List.of(), new LLMAgentListener() {
        }, () -> false);

        String plannerRequest = model.requestTexts.get(0);
        String workerRequest = model.requestTexts.get(1);
        assertTrue(plannerRequest.contains("host: 10.0.0.12"));
        assertTrue(plannerRequest.contains("user: root"));
        assertTrue(plannerRequest.contains("defaultTarget: the currently bound ssh session"));
        assertTrue(workerRequest.contains("host: 10.0.0.12"));
        assertTrue(workerRequest.contains("user: root"));
        assertTrue(workerRequest.contains("defaultTarget: the currently bound ssh session"));
        assertFalse(plannerRequest.toLowerCase().contains("password"));
        assertFalse(workerRequest.toLowerCase().contains("password"));
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
                "session tools", "internal tools", "local execution tools", "mcp tools",
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
                "session tools", "internal tools", "local execution tools", "mcp tools",
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

    @Test
    public void workerUsesConfiguredMessageWindow() {
        RoutingChatModel model = new RoutingChatModel("""
                {"objective":"query","steps":[
                  {"agent":"connection","objective":"query current connection"}
                ]}
                """);
        LLMAgentRuntime runtime = new LLMAgentRuntime(model, null,
                List.of(), null,
                "session tools", "internal tools", "local execution tools",
                null, "mcp tools", "execution rules", "language rules", null, 7);

        assertEquals(7, runtime.maxMessages());
    }

    @Test
    public void singleModeSkipsPlannerAndUsesAllSelectedToolScopes() throws Exception {
        SingleCallChatModel model = new SingleCallChatModel();
        List<LLMAgentEventType> events = new ArrayList<>();
        Map<String, LLMAgentTrace> traces = new LinkedHashMap<>();
        LLMSessionContext context = new LLMSessionContext(
                "tab-1", "server-a", "ssh", "10.0.0.12", 22, "root");
        LLMAgentRuntime runtime = new LLMAgentRuntime(model, null,
                List.of(new SessionEchoTools(), new EchoTools(LLMToolScope.INTERNAL),
                        new LocalEchoTools()), null,
                "session tools", "internal tools", "local execution tools",
                context, "mcp tools", "execution rules", "language rules",
                null, 7, LLMAgentMode.SINGLE);

        ChatResponse response = runtime.execute("simple task", List.of(),
                new LLMAgentListener() {
                    @Override
                    public void onEvent(LLMAgentEvent event) {
                        events.add(event.type());
                    }

                    @Override
                    public void onAgentTrace(LLMAgentTrace trace) {
                        traces.put(trace.id(), trace);
                    }
                }, () -> false);

        assertEquals("single direct result", response.aiMessage().text());
        assertEquals(1, model.calls);
        assertTrue(model.requestTools.containsAll(List.of(
                "session_echo", "echo_value", "local_echo")));
        assertFalse(model.requestText.contains("Create the minimum executable plan"));
        assertFalse(model.requestText.contains("Worker results:"));
        assertEquals(List.of(
                LLMAgentEventType.STEP_STARTED,
                LLMAgentEventType.STEP_COMPLETED,
                LLMAgentEventType.COMPLETED), events);
        assertEquals(LLMAgentTrace.Type.SINGLE, traces.get("single").type());
        assertEquals("single thinking", traces.get("single").thinking());
        assertEquals("single direct result", traces.get("single").conclusion());
    }

    @Test
    public void singleModeStreamsWithoutCallingPlannerOrVerifier() throws Exception {
        RoutingChatModel chatModel = new RoutingChatModel("""
                {"objective":"unused","steps":[]}
                """);
        LLMAgentRuntime runtime = new LLMAgentRuntime(chatModel,
                new StreamingWorkerModel(), List.of(), null,
                "session tools", "internal tools", "local execution tools",
                null, "mcp tools", "execution rules", "language rules",
                null, 7, LLMAgentMode.SINGLE);

        ChatResponse response = runtime.execute("simple stream", List.of(),
                new LLMAgentListener() {
                }, () -> false);

        assertEquals("streamed worker result", response.aiMessage().text());
        assertEquals(0, chatModel.workerCalls);
        assertFalse(chatModel.synthesisCalled);
    }

    @Test
    public void singleModeRetriesConnectionReset() throws Exception {
        ResettingStreamingModel streamingModel = new ResettingStreamingModel();
        LLMAgentRuntime runtime = new LLMAgentRuntime(
                new RoutingChatModel("unused"), streamingModel, List.of(), null,
                "session tools", "internal tools", "local execution tools",
                null, "mcp tools", "execution rules", "language rules",
                null, 20, LLMAgentMode.SINGLE);

        ChatResponse response = runtime.execute("retry stream", List.of(),
                new LLMAgentListener() {
                }, () -> false);

        assertEquals("recovered response", response.aiMessage().text());
        assertEquals(2, streamingModel.attempts.get());
    }

    @Test
    public void singleModeIncludesHighestPriorityPrompt() throws Exception {
        CapturingSystemChatModel chatModel = new CapturingSystemChatModel();
        LLMAgentRuntime runtime = new LLMAgentRuntime(
                chatModel, null, List.of(), null,
                "session tools", "internal tools", "local execution tools",
                null, "mcp tools", "execution rules", "language rules",
                null, 20, LLMAgentMode.SINGLE);
        runtime.setPromptSystemMessage("HIGH PRIORITY PROMPT");

        runtime.execute("prompt test", List.of(), new LLMAgentListener() {
        }, () -> false);

        assertTrue(chatModel.systemMessage.contains("HIGH PRIORITY PROMPT"));
    }

    @Test
    public void parallelOrchestrationRunsDifferentScopesConcurrently() throws Exception {
        ConcurrentPlanChatModel model = new ConcurrentPlanChatModel("""
                {"objective":"parallel","steps":[
                  {"id":"a","agent":"connection","objective":"query A","dependsOn":[]},
                  {"id":"b","agent":"local","objective":"query B","dependsOn":[]}
                ]}
                """, 120);
        LLMAgentRuntime runtime = orchestrationRuntime(model,
                List.of(new SessionEchoTools(), new LocalEchoTools()),
                LLMAgentOrchestrationMode.PARALLEL, 2,
                LLMAgentFailurePolicy.CONTINUE);

        ChatResponse response = runtime.execute("parallel task", List.of(),
                new LLMAgentListener() {
                }, () -> false);

        assertEquals("parallel final", response.aiMessage().text());
        assertEquals(2, model.workerCalls.get());
        assertEquals(2, model.maxActive.get());
    }

    @Test
    public void parallelOrchestrationSerializesSharedToolScope() throws Exception {
        ConcurrentPlanChatModel model = new ConcurrentPlanChatModel("""
                {"objective":"parallel","steps":[
                  {"id":"a","agent":"connection","objective":"query A","dependsOn":[]},
                  {"id":"b","agent":"connection","objective":"query B","dependsOn":[]}
                ]}
                """, 80);
        LLMAgentRuntime runtime = orchestrationRuntime(model,
                List.of(new SessionEchoTools()),
                LLMAgentOrchestrationMode.PARALLEL, 2,
                LLMAgentFailurePolicy.CONTINUE);

        runtime.execute("parallel task", List.of(), new LLMAgentListener() {
        }, () -> false);

        assertEquals(2, model.workerCalls.get());
        assertEquals(1, model.maxActive.get());
    }

    @Test
    public void parallelOrchestrationHonorsConfiguredConcurrencyLimit() throws Exception {
        ConcurrentPlanChatModel model = new ConcurrentPlanChatModel("""
                {"objective":"parallel","steps":[
                  {"id":"a","agent":"connection","objective":"query A","dependsOn":[]},
                  {"id":"b","agent":"local","objective":"query B","dependsOn":[]},
                  {"id":"c","agent":"internal","objective":"query C","dependsOn":[]}
                ]}
                """, 60);
        LLMAgentRuntime runtime = orchestrationRuntime(model,
                List.of(new SessionEchoTools(), new LocalEchoTools(),
                        new EchoTools(LLMToolScope.INTERNAL)),
                LLMAgentOrchestrationMode.PARALLEL, 2,
                LLMAgentFailurePolicy.CONTINUE);

        runtime.execute("parallel task", List.of(), new LLMAgentListener() {
        }, () -> false);

        assertEquals(3, model.workerCalls.get());
        assertEquals(2, model.maxActive.get());
    }

    @Test
    public void dagOrchestrationWaitsForDependencies() throws Exception {
        ConcurrentPlanChatModel model = new ConcurrentPlanChatModel("""
                {"objective":"dag","steps":[
                  {"id":"a","agent":"connection","objective":"query A","dependsOn":[]},
                  {"id":"b","agent":"local","objective":"query B","dependsOn":["a"]}
                ]}
                """, 40);
        LLMAgentRuntime runtime = orchestrationRuntime(model,
                List.of(new SessionEchoTools(), new LocalEchoTools()),
                LLMAgentOrchestrationMode.DAG, 2,
                LLMAgentFailurePolicy.CONTINUE);

        runtime.execute("dag task", List.of(), new LLMAgentListener() {
        }, () -> false);

        String dependentPrompt = model.prompts.stream()
                .filter(text -> text.contains("query B"))
                .filter(text -> text.contains("Current step"))
                .findFirst().orElseThrow();
        assertTrue(dependentPrompt.contains("query A"));
        assertEquals(1, model.maxActive.get());
    }

    @Test
    public void dagSkipsDependentStepWhenContinuePolicyIsUsed() throws Exception {
        DagDependencyFailureModel model = new DagDependencyFailureModel();
        LLMAgentRuntime runtime = orchestrationRuntime(model,
                List.of(new SessionEchoTools(), new LocalEchoTools()),
                LLMAgentOrchestrationMode.DAG, 2,
                LLMAgentFailurePolicy.CONTINUE);

        ChatResponse response = runtime.execute("dag failure", List.of(),
                new LLMAgentListener() {
                }, () -> false);

        assertEquals("dag failure final", response.aiMessage().text());
        assertEquals(1, model.workerCalls.get());
    }

    @Test
    public void dynamicSupervisorReplansAfterEachStep() throws Exception {
        DynamicSupervisorModel model = new DynamicSupervisorModel();
        LLMAgentRuntime runtime = orchestrationRuntime(model,
                List.of(new SessionEchoTools()),
                LLMAgentOrchestrationMode.DYNAMIC_SUPERVISOR, 0,
                LLMAgentFailurePolicy.CONTINUE);

        ChatResponse response = runtime.execute("dynamic task", List.of(),
                new LLMAgentListener() {
                }, () -> false);

        assertEquals("dynamic worker result", response.aiMessage().text());
        assertEquals(2, model.plannerCalls.get());
        assertEquals(1, model.workerCalls.get());
        assertTrue(model.prompts.stream()
                .filter(text -> text.contains("Current objective:"))
                .anyMatch(text -> text.contains("dynamic worker result")));
    }

    @Test
    public void retryPolicyRetriesFailedWorkerOnce() throws Exception {
        RetryPlanChatModel model = new RetryPlanChatModel();
        LLMAgentRuntime runtime = orchestrationRuntime(model,
                List.of(new SessionEchoTools()),
                LLMAgentOrchestrationMode.SEQUENTIAL, 2,
                LLMAgentFailurePolicy.RETRY);

        ChatResponse response = runtime.execute("retry task", List.of(),
                new LLMAgentListener() {
                }, () -> false);

        assertEquals("retry succeeded", response.aiMessage().text());
        assertEquals(2, model.workerCalls.get());
    }

    @Test(expected = IllegalStateException.class)
    public void failFastStopsAfterFailedWorker() throws Exception {
        FailFastPlanChatModel model = new FailFastPlanChatModel();
        LLMAgentRuntime runtime = orchestrationRuntime(model,
                List.of(new SessionEchoTools(), new LocalEchoTools()),
                LLMAgentOrchestrationMode.SEQUENTIAL, 2,
                LLMAgentFailurePolicy.FAIL_FAST);

        try {
            runtime.execute("fail task", List.of(), new LLMAgentListener() {
            }, () -> false);
        } finally {
            assertEquals(1, model.workerCalls.get());
        }
    }

    @Test
    public void continuePolicyRunsIndependentStepsAfterFailure() throws Exception {
        ContinueFailurePlanChatModel model = new ContinueFailurePlanChatModel();
        LLMAgentRuntime runtime = orchestrationRuntime(model,
                List.of(new SessionEchoTools(), new LocalEchoTools()),
                LLMAgentOrchestrationMode.SEQUENTIAL, 2,
                LLMAgentFailurePolicy.CONTINUE);

        ChatResponse response = runtime.execute("continue task", List.of(),
                new LLMAgentListener() {
                }, () -> false);

        assertEquals("continued final", response.aiMessage().text());
        assertEquals(2, model.workerCalls.get());
    }

    @Test
    public void configuredMaximumStepsAllowsPlansBeyondSixSteps() throws Exception {
        ConfiguredStepLimitChatModel model = new ConfiguredStepLimitChatModel();
        LLMAgentRuntime runtime = new LLMAgentRuntime(model, null,
                List.of(new SessionEchoTools()), null,
                "session tools", "internal tools", "local execution tools",
                null, "mcp tools", "execution rules", "language rules",
                null, 20, LLMAgentMode.MULTI,
                LLMAgentOrchestrationMode.SEQUENTIAL, 2, 20,
                LLMAgentFailurePolicy.CONTINUE);

        runtime.execute("many steps", List.of(), new LLMAgentListener() {
        }, () -> false);

        assertEquals(8, model.workerCalls.get());
        assertTrue(model.plannerPrompt.contains("Maximum 20 steps"));
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
                "session tools", "internal tools", "local execution tools", "mcp tools",
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
                        LLMAgentTrace.Type.WORKER, 0, 1, "connection", "query",
                        LLMAgentOrchestrationMode.PARALLEL)
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
        assertTrue(trace.contains(LLMAgentOrchestrationMode.PARALLEL.getDisplayName()));
        assertTrue(trace.contains("verifier thinking"));
        assertTrue(trace.contains("final answer"));
    }

    @Test
    public void traceFormatterCanHideThinkingWithoutHidingResults() {
        LLMAgentTrace worker = LLMAgentTrace.running("worker-0",
                        LLMAgentTrace.Type.WORKER, 0, 1, "connection", "query")
                .withThinking("private reasoning")
                .complete("visible result");

        String formatted = LLMAgentTraceFormatter.format(List.of(worker));
        String hidden = LLMAgentTraceFormatter.hideThinking(formatted);

        assertTrue(formatted.contains("private reasoning"));
        assertFalse(hidden.contains("private reasoning"));
        assertTrue(hidden.contains("visible result"));
        assertFalse(hidden.contains("easyshell-thinking"));
    }

    private static LLMAgentRuntime runtime(ChatModel model) {
        return new LLMAgentRuntime(model, null, List.of(), null,
                "session tools", "internal tools", "local execution tools", "mcp tools",
                "execution rules", "language rules", null);
    }

    private static LLMAgentRuntime orchestrationRuntime(ChatModel model,
                                                        List<LLMTools> tools,
                                                        LLMAgentOrchestrationMode mode,
                                                        int maxConcurrency,
                                                        LLMAgentFailurePolicy failurePolicy) {
        return new LLMAgentRuntime(model, null, tools, null,
                "session tools", "internal tools", "local execution tools",
                null, "mcp tools", "execution rules", "language rules",
                null, 20, LLMAgentMode.MULTI, mode, maxConcurrency,
                LLMAgentStepLimit.DEFAULT, failurePolicy);
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
            handler.onPartialResponse("streamed worker result");
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

    private static final class ResettingStreamingModel implements StreamingChatModel {

        private final AtomicInteger attempts = new AtomicInteger();

        @Override
        public void doChat(ChatRequest request, StreamingChatResponseHandler handler) {
            if (this.attempts.incrementAndGet() == 1) {
                handler.onError(new SocketException("Connection reset"));
                return;
            }
            handler.onPartialResponse("recovered response");
            handler.onCompleteResponse(ChatResponse.builder()
                    .aiMessage(AiMessage.from("recovered response"))
                    .build());
        }
    }

    private static final class CapturingSystemChatModel implements ChatModel {

        private String systemMessage = "";

        @Override
        public ChatResponse doChat(ChatRequest request) {
            this.systemMessage = request.messages().stream()
                    .filter(SystemMessage.class::isInstance)
                    .map(SystemMessage.class::cast)
                    .map(SystemMessage::text)
                    .collect(java.util.stream.Collectors.joining("\n"));
            return RoutingChatModel.response("prompt captured");
        }
    }

    private static final class ToolAwareChatModel implements ChatModel {

        private final String plan;

        private final boolean executeTool;

        private final List<List<String>> requestTools = new ArrayList<>();

        private final List<String> requestTexts = new ArrayList<>();

        private ToolAwareChatModel(String plan, boolean executeTool) {
            this.plan = plan;
            this.executeTool = executeTool;
        }

        @Override
        public ChatResponse doChat(ChatRequest request) {
            this.requestTexts.add(request.messages().stream()
                    .map(Object::toString)
                    .collect(java.util.stream.Collectors.joining("\n")));
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

    private static final class SingleCallChatModel implements ChatModel {

        private int calls;

        private List<String> requestTools = List.of();

        private String requestText = "";

        @Override
        public ChatResponse doChat(ChatRequest request) {
            this.calls++;
            this.requestTools = request.toolSpecifications() == null
                    ? List.of()
                    : request.toolSpecifications().stream()
                    .map(ToolSpecification::name)
                    .toList();
            this.requestText = request.messages().stream()
                    .map(Object::toString)
                    .collect(java.util.stream.Collectors.joining("\n"));
            return RoutingChatModel.response("single direct result", "single thinking");
        }
    }

    private static final class ConcurrentPlanChatModel implements ChatModel {

        private final String plan;

        private final long workerDelayMillis;

        private final AtomicInteger workerCalls = new AtomicInteger();

        private final AtomicInteger active = new AtomicInteger();

        private final AtomicInteger maxActive = new AtomicInteger();

        private final List<String> prompts = new CopyOnWriteArrayList<>();

        private ConcurrentPlanChatModel(String plan, long workerDelayMillis) {
            this.plan = plan;
            this.workerDelayMillis = workerDelayMillis;
        }

        @Override
        public ChatResponse doChat(ChatRequest request) {
            String prompt = request.messages().stream()
                    .map(Object::toString)
                    .collect(java.util.stream.Collectors.joining("\n"));
            this.prompts.add(prompt);
            if (prompt.contains("Create the minimum executable plan")) {
                return RoutingChatModel.response(this.plan);
            }
            if (prompt.contains("Worker results:")) {
                return RoutingChatModel.response("parallel final");
            }
            if (prompt.contains("Current step ")) {
                this.workerCalls.incrementAndGet();
                int current = this.active.incrementAndGet();
                this.maxActive.accumulateAndGet(current, Math::max);
                try {
                    Thread.sleep(this.workerDelayMillis);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } finally {
                    this.active.decrementAndGet();
                }
                return RoutingChatModel.response("worker result");
            }
            return RoutingChatModel.response("unexpected");
        }
    }

    private static final class DynamicSupervisorModel implements ChatModel {

        private final AtomicInteger plannerCalls = new AtomicInteger();

        private final AtomicInteger workerCalls = new AtomicInteger();

        private final List<String> prompts = new CopyOnWriteArrayList<>();

        @Override
        public ChatResponse doChat(ChatRequest request) {
            String prompt = request.messages().stream()
                    .map(Object::toString)
                    .collect(java.util.stream.Collectors.joining("\n"));
            this.prompts.add(prompt);
            if (prompt.contains("Current objective:") && prompt.contains("Completed step results:")) {
                this.plannerCalls.incrementAndGet();
                if (prompt.contains("Completed step results:\n(none)")) {
                    return RoutingChatModel.response("""
                            {"done":false,"objective":"dynamic task","agent":"connection",
                             "stepObjective":"run dynamic worker","reason":"execute"}
                            """);
                }
                return RoutingChatModel.response(
                        "{\"done\":true,\"objective\":\"dynamic task\"}");
            }
            if (prompt.contains("Current step ")) {
                this.workerCalls.incrementAndGet();
                return RoutingChatModel.response("dynamic worker result");
            }
            return RoutingChatModel.response("unexpected");
        }
    }

    private static final class RetryPlanChatModel implements ChatModel {

        private final AtomicInteger workerCalls = new AtomicInteger();

        @Override
        public ChatResponse doChat(ChatRequest request) {
            String prompt = request.messages().stream()
                    .map(Object::toString)
                    .collect(java.util.stream.Collectors.joining("\n"));
            if (prompt.contains("Create the minimum executable plan")) {
                return RoutingChatModel.response("""
                        {"objective":"retry","steps":[
                          {"id":"a","agent":"connection","objective":"retry worker"}
                        ]}
                        """);
            }
            if (prompt.contains("Current step ")) {
                int call = this.workerCalls.incrementAndGet();
                return RoutingChatModel.response(call == 1 ? "" : "retry succeeded");
            }
            return RoutingChatModel.response("unexpected");
        }
    }

    private static final class FailFastPlanChatModel implements ChatModel {

        private final AtomicInteger workerCalls = new AtomicInteger();

        @Override
        public ChatResponse doChat(ChatRequest request) {
            String prompt = request.messages().stream()
                    .map(Object::toString)
                    .collect(java.util.stream.Collectors.joining("\n"));
            if (prompt.contains("Create the minimum executable plan")) {
                return RoutingChatModel.response("""
                        {"objective":"fail","steps":[
                          {"id":"a","agent":"connection","objective":"fail worker"},
                          {"id":"b","agent":"local","objective":"must not run"}
                        ]}
                        """);
            }
            if (prompt.contains("Current step ")) {
                this.workerCalls.incrementAndGet();
                return RoutingChatModel.response("");
            }
            return RoutingChatModel.response("unexpected");
        }
    }

    private static final class ContinueFailurePlanChatModel implements ChatModel {

        private final AtomicInteger workerCalls = new AtomicInteger();

        @Override
        public ChatResponse doChat(ChatRequest request) {
            String prompt = request.messages().stream()
                    .map(Object::toString)
                    .collect(java.util.stream.Collectors.joining("\n"));
            if (prompt.contains("Create the minimum executable plan")) {
                return RoutingChatModel.response("""
                        {"objective":"continue","steps":[
                          {"id":"a","agent":"connection","objective":"failed worker"},
                          {"id":"b","agent":"local","objective":"independent worker"}
                        ]}
                        """);
            }
            if (prompt.contains("Current step ")) {
                int call = this.workerCalls.incrementAndGet();
                return RoutingChatModel.response(call == 1 ? "" : "independent result");
            }
            if (prompt.contains("Worker results:")) {
                return RoutingChatModel.response("continued final");
            }
            return RoutingChatModel.response("unexpected");
        }
    }

    private static final class DagDependencyFailureModel implements ChatModel {

        private final AtomicInteger workerCalls = new AtomicInteger();

        @Override
        public ChatResponse doChat(ChatRequest request) {
            String prompt = request.messages().stream()
                    .map(Object::toString)
                    .collect(java.util.stream.Collectors.joining("\n"));
            if (prompt.contains("Create the minimum executable plan")) {
                return RoutingChatModel.response("""
                        {"objective":"dag failure","steps":[
                          {"id":"a","agent":"connection","objective":"failed dependency","dependsOn":[]},
                          {"id":"b","agent":"local","objective":"blocked dependent","dependsOn":["a"]}
                        ]}
                        """);
            }
            if (prompt.contains("Current step ")) {
                this.workerCalls.incrementAndGet();
                return RoutingChatModel.response("");
            }
            if (prompt.contains("Worker results:")) {
                return RoutingChatModel.response("dag failure final");
            }
            return RoutingChatModel.response("unexpected");
        }
    }

    private static final class ConfiguredStepLimitChatModel implements ChatModel {

        private final AtomicInteger workerCalls = new AtomicInteger();

        private String plannerPrompt = "";

        @Override
        public ChatResponse doChat(ChatRequest request) {
            String prompt = request.messages().stream()
                    .map(Object::toString)
                    .collect(java.util.stream.Collectors.joining("\n"));
            if (prompt.contains("Create the minimum executable plan")) {
                this.plannerPrompt = prompt;
                return RoutingChatModel.response("""
                        {"objective":"many","steps":[
                          {"id":"a","agent":"connection","objective":"step A"},
                          {"id":"b","agent":"connection","objective":"step B"},
                          {"id":"c","agent":"connection","objective":"step C"},
                          {"id":"d","agent":"connection","objective":"step D"},
                          {"id":"e","agent":"connection","objective":"step E"},
                          {"id":"f","agent":"connection","objective":"step F"},
                          {"id":"g","agent":"connection","objective":"step G"},
                          {"id":"h","agent":"connection","objective":"step H"}
                        ]}
                        """);
            }
            if (prompt.contains("Current step ")) {
                this.workerCalls.incrementAndGet();
                return RoutingChatModel.response("worker result");
            }
            if (prompt.contains("Worker results:")) {
                return RoutingChatModel.response("many steps final");
            }
            return RoutingChatModel.response("unexpected");
        }
    }

    private static final class EchoTools implements LLMTools {

        private int calls;

        private final LLMToolScope scope;

        private EchoTools() {
            this(LLMToolScope.SESSION);
        }

        private EchoTools(LLMToolScope scope) {
            this.scope = scope;
        }

        @Tool(name = "echo_value", value = "Echo a value")
        public String echo(@P("value to echo") String value) {
            this.calls++;
            return "echo:" + value;
        }

        @Override
        public LLMToolScope scope() {
            return this.scope;
        }

        @Override
        public String systemMessage(LLMExecutionMode executionMode) {
            return "Echo tools";
        }
    }

    private static final class SessionEchoTools implements LLMTools {

        @Tool(name = "session_echo", value = "Echo a session value")
        public String echo() {
            return "session";
        }

        @Override
        public String systemMessage(LLMExecutionMode executionMode) {
            return "Session tools";
        }
    }

    private static final class LocalEchoTools implements LLMTools {

        @Tool(name = "local_echo", value = "Echo a local value")
        public String echo() {
            return "local";
        }

        @Override
        public LLMToolScope scope() {
            return LLMToolScope.LOCAL_EXECUTION;
        }

        @Override
        public String systemMessage(LLMExecutionMode executionMode) {
            return "Local tools";
        }
    }
}
