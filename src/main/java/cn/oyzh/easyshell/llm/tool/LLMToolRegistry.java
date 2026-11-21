package cn.oyzh.easyshell.llm.tool;

import dev.langchain4j.agent.tool.ReturnBehavior;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.invocation.InvocationContext;
import dev.langchain4j.internal.Utils;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.AiServiceTool;
import dev.langchain4j.service.tool.DefaultToolExecutor;
import dev.langchain4j.service.tool.ToolExecutionResult;
import dev.langchain4j.service.tool.ToolExecutor;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.service.tool.ToolProviderRequest;
import dev.langchain4j.service.tool.ToolProviderResult;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Registers static and dynamic tools behind one runtime policy.
 *
 * @author oyzh
 * @since 2026-11-21
 */
public final class LLMToolRegistry {

    private LLMToolRegistry() {
    }

    /**
     * Configures tools and tool providers with runtime policy enforcement.
     *
     * @param services         AI service builder
     * @param tools            ordinary EasyShell tools
     * @param dynamicProviders MCP and Skill providers
     * @param policy           runtime policy
     */
    public static <T> void configure(AiServices<T> services,
                                     Collection<LLMTools> tools,
                                     Collection<ToolProvider> dynamicProviders,
                                     LLMToolRuntimePolicy policy) {
        List<ToolProvider> providers = new ArrayList<>();
        if (tools != null && !tools.isEmpty()) {
            providers.add(staticProvider(tools, policy));
        }
        if (dynamicProviders != null) {
            for (ToolProvider provider : dynamicProviders) {
                if (provider != null) {
                    providers.add(policyProvider(provider, policy));
                }
            }
        }
        if (!providers.isEmpty()) {
            services.toolProviders(providers);
        }
    }

    /**
     * Resolves ordinary tool name to scope mappings.
     *
     * @param tools ordinary tools
     * @return scope mappings
     */
    public static Map<String, LLMToolScope> scopes(Collection<LLMTools> tools) {
        Map<String, LLMToolScope> result = new LinkedHashMap<>();
        if (tools == null) {
            return result;
        }
        for (LLMTools tool : tools) {
            if (tool == null) {
                continue;
            }
            for (ToolSpecification specification :
                    ToolSpecifications.toolSpecificationsFrom(tool)) {
                result.put(specification.name(), tool.scope());
            }
        }
        return result;
    }

    /**
     * Adds policy rejection and blocked-audit handling for tools hidden from the current request.
     *
     * @param delegate base hallucinated-tool strategy
     * @param policy   runtime policy
     * @param audit    tool audit, can be null
     * @return protected strategy
     */
    public static Function<ToolExecutionRequest, ToolExecutionResultMessage>
    protectHallucinatedStrategy(
            Function<ToolExecutionRequest, ToolExecutionResultMessage> delegate,
            LLMToolRuntimePolicy policy,
            LLMToolAudit audit) {
        return request -> {
            LLMToolScope scope = policy.scopeOf(request.name());
            if (scope != null && !policy.exposes(scope)) {
                LLMToolRuntimePolicy.Decision decision = policy.check(request.name(), scope);
                if (!decision.allowed()) {
                    if (audit != null) {
                        audit.recordBlocked(request.name(),
                                stripBlockedPrefix(decision.blockReason()));
                    }
                    return ToolExecutionResultMessage.builder()
                            .id(request.id())
                            .toolName(request.name())
                            .text(decision.blockReason())
                            .isError(true)
                            .build();
                }
            }
            return delegate.apply(request);
        };
    }

    private static String stripBlockedPrefix(String reason) {
        return reason != null && reason.startsWith(LLMToolRuntimePolicy.BLOCKED_PREFIX)
                ? reason.substring(LLMToolRuntimePolicy.BLOCKED_PREFIX.length()).trim()
                : reason;
    }

    private static ToolProvider staticProvider(Collection<LLMTools> tools,
                                               LLMToolRuntimePolicy policy) {
        ToolProviderResult result = staticResult(tools, policy);
        return request -> result;
    }

    static ToolProviderResult staticResult(Collection<LLMTools> tools,
                                           LLMToolRuntimePolicy policy) {
        List<AiServiceTool> registered = new ArrayList<>();
        for (LLMTools tool : tools) {
            if (tool == null || !policy.exposes(tool.scope())) {
                continue;
            }
            registered.addAll(staticTools(tool, policy));
        }
        return result(registered);
    }

    private static List<AiServiceTool> staticTools(LLMTools tools,
                                                   LLMToolRuntimePolicy policy) {
        Map<String, ToolMethod> methods = methods(tools);
        List<AiServiceTool> result = new ArrayList<>();
        for (ToolSpecification specification :
                ToolSpecifications.toolSpecificationsFrom(tools)) {
            ToolMethod toolMethod = methods.get(specification.name());
            if (toolMethod == null) {
                throw new IllegalStateException(
                        "No @Tool method found for specification: " + specification.name());
            }
            Tool annotation = toolMethod.original().getAnnotation(Tool.class);
            ReturnBehavior returnBehavior = annotation.returnBehavior();
            ToolExecutor executor = DefaultToolExecutor.builder()
                    .object(tools)
                    .originalMethod(toolMethod.original())
                    .methodToInvoke(toolMethod.invoke())
                    .wrapToolArgumentsExceptions(true)
                    .propagateToolExecutionExceptions(true)
                    .build();
            result.add(AiServiceTool.builder()
                    .toolSpecification(specification)
                    .toolExecutor(new PolicyExecutor(executor, specification.name(),
                            tools.scope(), policy))
                    .returnBehavior(returnBehavior)
                    .build());
        }
        return result;
    }

    private static Map<String, ToolMethod> methods(LLMTools tools) {
        Map<String, ToolMethod> result = new LinkedHashMap<>();
        for (Method method : Utils.allConcreteMethods(tools.getClass())) {
            Method annotated = Utils.getAnnotatedMethod(method, Tool.class).orElse(null);
            if (annotated == null) {
                continue;
            }
            result.put(ToolSpecifications.toolNameFrom(annotated),
                    new ToolMethod(annotated, method));
        }
        return result;
    }

    private record ToolMethod(Method original, Method invoke) {
    }

    static ToolProvider policyProvider(ToolProvider delegate,
                                       LLMToolRuntimePolicy policy) {
        return request -> {
            ToolProviderResult source = delegate.provideTools(request);
            List<AiServiceTool> tools = new ArrayList<>();
            Set<String> immediate = new LinkedHashSet<>();
            for (AiServiceTool tool : source.aiServiceTools()) {
                LLMToolScope scope = policy.scopeOf(tool.name());
                if (!policy.exposes(scope)) {
                    continue;
                }
                ToolExecutor executor = new PolicyExecutor(tool.toolExecutor(), tool.name(),
                        scope, policy);
                tools.add(AiServiceTool.builder()
                        .toolSpecification(tool.toolSpecification())
                        .toolExecutor(executor)
                        .returnBehavior(tool.returnBehavior())
                        .build());
                if (source.immediateReturnToolNames().contains(tool.name())) {
                    immediate.add(tool.name());
                }
            }
            ToolProviderResult.Builder builder = ToolProviderResult.builder();
            for (AiServiceTool tool : tools) {
                builder.add(tool);
            }
            return builder.immediateReturnToolNames(immediate).build();
        };
    }

    private static ToolProviderResult result(List<AiServiceTool> tools) {
        ToolProviderResult.Builder builder = ToolProviderResult.builder();
        for (AiServiceTool tool : tools) {
            builder.add(tool);
        }
        return builder.build();
    }

    record PolicyExecutor(ToolExecutor delegate,
                          String toolName,
                          LLMToolScope scope,
                          LLMToolRuntimePolicy policy) implements ToolExecutor {

        @Override
        public String execute(ToolExecutionRequest request, Object memoryId) {
            LLMToolRuntimePolicy.Decision decision = this.policy.check(this.toolName, this.scope);
            return decision.allowed()
                    ? this.delegate.execute(request, memoryId)
                    : decision.blockReason();
        }

        @Override
        public ToolExecutionResult executeWithContext(ToolExecutionRequest request,
                                                      InvocationContext context) {
            LLMToolRuntimePolicy.Decision decision = this.policy.check(this.toolName, this.scope);
            return decision.allowed()
                    ? this.delegate.executeWithContext(request, context)
                    : denied(decision.blockReason());
        }

        @Override
        public CompletableFuture<ToolExecutionResult> executeAsync(
                ToolExecutionRequest request, InvocationContext context) {
            LLMToolRuntimePolicy.Decision decision = this.policy.check(this.toolName, this.scope);
            return decision.allowed()
                    ? this.delegate.executeAsync(request, context)
                    : CompletableFuture.completedFuture(denied(decision.blockReason()));
        }

        private static ToolExecutionResult denied(String reason) {
            return ToolExecutionResult.builder()
                    .isError(true)
                    .resultText(reason)
                    .build();
        }
    }
}
