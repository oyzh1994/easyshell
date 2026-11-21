package cn.oyzh.easyshell.llm.tool;

import cn.oyzh.easyshell.llm.LLMExecutionMode;
import cn.oyzh.easyshell.llm.session.LLMSessionContext;
import cn.oyzh.easyshell.local.ShellLocalExecutionLLMTools;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.invocation.InvocationContext;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolExecutionResult;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.service.tool.ToolProviderResult;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LLMToolRuntimePolicyTest {

    @Test
    public void nonLocalSessionHidesAndBlocksLocalExecutionWithoutExplicitLocalIntent() {
        SessionTools session = new SessionTools();
        LocalTools local = new LocalTools();
        List<LLMTools> tools = List.of(session, local);
        LLMToolRuntimePolicy policy = policy(LLMExecutionMode.APPROVE,
                LLMToolAccessMode.SESSION_WITH_LOCAL, "安装 Docker", tools);

        assertFalse(policy.exposes(LLMToolScope.LOCAL_EXECUTION));
        assertTrue(policy.exposes(LLMToolScope.SESSION));
        LLMToolRuntimePolicy.Decision decision = policy.check(
                "local_execute_command", LLMToolScope.LOCAL_EXECUTION);
        assertFalse(decision.allowed());
        assertTrue(decision.blockReason().startsWith(LLMToolRuntimePolicy.BLOCKED_PREFIX));
        assertTrue(decision.blockReason().contains("ssh_execute_command"));
        assertTrue(decision.blockReason().contains("本机/本地"));

        ToolProviderResult names = LLMToolRegistry.staticResult(tools, policy);
        assertEquals(1, names.aiServiceTools().size());
        assertEquals("ssh_execute_command", names.aiServiceTools().getFirst().name());
        assertEquals(0, local.calls.get());
    }

    @Test
    public void explicitLocalIntentAllowsLocalExecution() {
        LocalTools local = new LocalTools();
        LLMToolRuntimePolicy policy = policy(LLMExecutionMode.APPROVE,
                LLMToolAccessMode.SESSION_WITH_LOCAL,
                "在本机安装 Docker", List.of(new SessionTools(), local));

        assertTrue(policy.exposes(LLMToolScope.LOCAL_EXECUTION));
        assertTrue(policy.check("local_execute_command",
                LLMToolScope.LOCAL_EXECUTION).allowed());
    }

    @Test
    public void localSessionExposesSessionAndLocalExecution() {
        LocalTools local = new LocalTools();
        LLMToolRuntimePolicy policy = new LLMToolRuntimePolicy(
                LLMExecutionMode.APPROVE,
                LLMToolAccessMode.SESSION_WITH_LOCAL,
                new LLMSessionContext("tab-local", "local", "LOCAL",
                        "localhost", 0, System.getProperty("user.name")),
                "安装 Docker",
                LLMToolRegistry.scopes(List.of(new SessionTools(), local)));

        assertTrue(policy.exposes(LLMToolScope.SESSION));
        assertTrue(policy.exposes(LLMToolScope.LOCAL_EXECUTION));
        assertTrue(policy.check("local_execute_command",
                LLMToolScope.LOCAL_EXECUTION).allowed());
    }

    @Test
    public void suggestModeExposesNoTools() {
        LocalTools local = new LocalTools();
        LLMToolRuntimePolicy policy = policy(LLMExecutionMode.SUGGEST,
                LLMToolAccessMode.COMPLETE, "安装 Docker",
                List.of(new SessionTools(), new InternalTools(), local));

        assertFalse(policy.exposes(LLMToolScope.SESSION));
        assertFalse(policy.exposes(LLMToolScope.LOCAL_EXECUTION));
        assertFalse(policy.exposes(LLMToolScope.INTERNAL));
        assertTrue(LLMToolRegistry.staticResult(List.of(local), policy)
                .aiServiceTools().isEmpty());
        assertTrue(policy.check("local_execute_command",
                LLMToolScope.LOCAL_EXECUTION).blockReason().contains("Suggest"));
    }

    @Test
    public void sessionPolicyKeepsMcpAndSkillScopesIndependent() {
        LLMToolRuntimePolicy policy = policy(LLMExecutionMode.APPROVE,
                LLMToolAccessMode.SESSION_ONLY, "查看服务器时间",
                List.of(new SessionTools(), new LocalTools()));

        assertTrue(policy.exposes(LLMToolScope.MCP));
        assertTrue(policy.exposes(LLMToolScope.SKILL));
        assertFalse(policy.exposes(LLMToolScope.LOCAL_EXECUTION));
    }

    @Test
    public void sessionOnlyModeBlocksLocalExecution() {
        LLMToolRuntimePolicy policy = policy(LLMExecutionMode.APPROVE,
                LLMToolAccessMode.SESSION_ONLY, "在本机安装 Docker",
                List.of(new SessionTools(), new LocalTools()));

        assertFalse(policy.exposes(LLMToolScope.LOCAL_EXECUTION));
        assertFalse(policy.check("local_execute_command",
                LLMToolScope.LOCAL_EXECUTION).allowed());
    }

    @Test
    public void policyExecutorReturnsErrorWithoutInvokingDelegate() {
        SessionTools session = new SessionTools();
        LocalTools local = new LocalTools();
        LLMToolRuntimePolicy policy = policy(LLMExecutionMode.APPROVE,
                LLMToolAccessMode.SESSION_WITH_LOCAL, "安装 Docker",
                List.of(session, local));
        AtomicInteger delegateCalls = new AtomicInteger();
        LLMToolRegistry.PolicyExecutor executor = new LLMToolRegistry.PolicyExecutor(
                (request, memoryId) -> {
                    delegateCalls.incrementAndGet();
                    return "executed";
                }, "local_execute_command", LLMToolScope.LOCAL_EXECUTION, policy);

        ToolExecutionResult result = executor.executeWithContext(
                ToolExecutionRequest.builder()
                        .id("local-1")
                        .name("local_execute_command")
                        .arguments("{\"command\":\"id\"}")
                        .build(),
                (InvocationContext) null);

        assertTrue(result.isError());
        assertTrue(result.resultText().contains("Execution blocked"));
        assertEquals(0, delegateCalls.get());
    }

    @Test
    public void registryInvokesExposedStaticToolThroughAiServices() {
        SessionTools session = new SessionTools();
        LLMToolRuntimePolicy policy = policy(LLMExecutionMode.APPROVE,
                LLMToolAccessMode.SESSION_WITH_LOCAL, "查看服务器时间",
                List.of(session, new LocalTools()));
        AtomicReference<String> toolResult = new AtomicReference<>();
        AiServices<Assistant> services = AiServices.builder(Assistant.class)
                .chatModel(new ForcedToolModel("ssh_execute_command", toolResult));
        LLMToolRegistry.configure(services, List.of(session),
                List.of(), policy);

        assertEquals("done", services.build().chat("run"));
        assertEquals(1, session.calls.get());
        assertEquals("ok", toolResult.get());
    }

    @Test
    public void registryResolvesRealLocalExecutionToolSpecifications() {
        try (ShellLocalExecutionLLMTools tools = new ShellLocalExecutionLLMTools()) {
            LLMToolRuntimePolicy policy = new LLMToolRuntimePolicy(
                    LLMExecutionMode.APPROVE,
                    LLMToolAccessMode.SESSION_WITH_LOCAL,
                    null, "查看本机 IP",
                    LLMToolRegistry.scopes(List.of(tools)));

            ToolProviderResult result = LLMToolRegistry.staticResult(List.of(tools), policy);

            assertTrue(result.aiServiceTools().stream()
                    .anyMatch(tool -> tool.name().equals("local_get_public_ip")));
            assertTrue(result.aiServiceTools().stream()
                    .anyMatch(tool -> tool.name().equals("local_execute_command_direct")));
        }
    }

    @Test
    public void suggestModeFiltersDynamicMcpAndSkillProviders() {
        LLMToolRuntimePolicy policy = new LLMToolRuntimePolicy(
                LLMExecutionMode.SUGGEST,
                LLMToolAccessMode.COMPLETE,
                null, "生成配置文件",
                Map.of("dynamic_skill_tool", LLMToolScope.SKILL));
        ToolSpecification specification = ToolSpecification.builder()
                .name("dynamic_skill_tool")
                .description("Dynamic Skill tool")
                .build();
        ToolProvider delegate = request -> ToolProviderResult.builder()
                .add(specification, (toolRequest, memoryId) -> "executed")
                .build();

        ToolProviderResult result = LLMToolRegistry.policyProvider(delegate, policy)
                .provideTools(null);

        assertTrue(result.aiServiceTools().isEmpty());
    }

    private static LLMToolRuntimePolicy policy(LLMExecutionMode executionMode,
                                               LLMToolAccessMode accessMode,
                                               String task,
                                               List<LLMTools> tools) {
        LLMSessionContext session = new LLMSessionContext(
                "tab-ssh", "server-a", "SSH", "10.0.0.12", 22, "root");
        Map<String, LLMToolScope> scopes = LLMToolRegistry.scopes(tools);
        return new LLMToolRuntimePolicy(executionMode, accessMode, session, task, scopes);
    }

    private static final class SessionTools implements LLMTools {
        private final AtomicInteger calls = new AtomicInteger();

        @Override
        public LLMToolScope scope() {
            return LLMToolScope.SESSION;
        }

        @Tool(name = "ssh_execute_command")
        public String executeCommand(String command) {
            this.calls.incrementAndGet();
            return "ok";
        }

        @Override
        public String systemMessage(LLMExecutionMode executionMode) {
            return "";
        }
    }

    private static final class LocalTools implements LLMTools {
        private final AtomicInteger calls = new AtomicInteger();

        @Override
        public LLMToolScope scope() {
            return LLMToolScope.LOCAL_EXECUTION;
        }

        @Tool(name = "local_execute_command")
        public String executeCommand(String command) {
            this.calls.incrementAndGet();
            return "ok";
        }

        @Override
        public String systemMessage(LLMExecutionMode executionMode) {
            return "";
        }
    }

    private static final class InternalTools implements LLMTools {

        @Override
        public LLMToolScope scope() {
            return LLMToolScope.INTERNAL;
        }

        @Tool(name = "internal_get_version")
        public String version() {
            return "1.0";
        }

        @Override
        public String systemMessage(LLMExecutionMode executionMode) {
            return "";
        }
    }

    private interface Assistant {

        String chat(String message);
    }

    private record ForcedToolModel(String toolName,
                                   AtomicReference<String> result) implements ChatModel {

        @Override
        public ChatResponse doChat(ChatRequest request) {
            ToolExecutionResultMessage toolResult = request.messages().stream()
                    .filter(ToolExecutionResultMessage.class::isInstance)
                    .map(ToolExecutionResultMessage.class::cast)
                    .findFirst()
                    .orElse(null);
            if (toolResult != null) {
                this.result.set(toolResult.text());
                return ChatResponse.builder()
                        .aiMessage(AiMessage.from("done"))
                        .build();
            }
            ToolExecutionRequest toolRequest = ToolExecutionRequest.builder()
                    .id("tool-1")
                    .name(this.toolName)
                    .arguments("{\"command\":\"date\"}")
                    .build();
            return ChatResponse.builder()
                    .aiMessage(AiMessage.from(toolRequest))
                    .build();
        }
    }
}
