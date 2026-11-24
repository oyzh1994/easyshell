package cn.oyzh.easyshell.controller.llm;

import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.controller.ShellMainController;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.llm.LLMView;
import cn.oyzh.easyshell.internal.ShellLLMTools;
import cn.oyzh.easyshell.llm.LLMAssistant;
import cn.oyzh.easyshell.llm.LLMExecutionMode;
import cn.oyzh.easyshell.llm.LLMContextBudgetUtil;
import cn.oyzh.easyshell.llm.LLMFileContentUtil;
import cn.oyzh.easyshell.llm.LLMFileContentType;
import cn.oyzh.easyshell.llm.LLMMessage;
import cn.oyzh.easyshell.llm.LLMParameterUtil;
import cn.oyzh.easyshell.llm.LLMNetworkRetry;
import cn.oyzh.easyshell.llm.LLMRequestTimeout;
import cn.oyzh.easyshell.llm.LLMResponseUtil;
import cn.oyzh.easyshell.llm.LLMSafeChatMemory;
import cn.oyzh.easyshell.llm.LLMTabCapability;
import cn.oyzh.easyshell.llm.LLMThinkingLevel;
import cn.oyzh.easyshell.llm.LLMTokenUsageStats;
import cn.oyzh.easyshell.llm.LLMTokenUsageTracker;
import cn.oyzh.easyshell.llm.LLMTokenAwareChatMemory;
import cn.oyzh.easyshell.llm.LLMUtil;
import cn.oyzh.easyshell.llm.agent.LLMAgentEvent;
import cn.oyzh.easyshell.llm.agent.LLMAgentEventType;
import cn.oyzh.easyshell.llm.agent.LLMAgentFailurePolicy;
import cn.oyzh.easyshell.llm.agent.LLMAgentListener;
import cn.oyzh.easyshell.llm.agent.LLMAgentMode;
import cn.oyzh.easyshell.llm.agent.LLMAgentOrchestrationMode;
import cn.oyzh.easyshell.llm.agent.LLMAgentRuntime;
import cn.oyzh.easyshell.llm.agent.LLMAgentTrace;
import cn.oyzh.easyshell.llm.agent.LLMAgentTraceFormatter;
import cn.oyzh.easyshell.llm.capability.LLMModelCapability;
import cn.oyzh.easyshell.llm.mcp.LLMMcpServer;
import cn.oyzh.easyshell.llm.mcp.LLMMcpToolSession;
import cn.oyzh.easyshell.llm.provider.LLMProvider;
import cn.oyzh.easyshell.llm.prompt.LLMPrompt;
import cn.oyzh.easyshell.llm.prompt.LLMPromptUtil;
import cn.oyzh.easyshell.llm.session.LLMSession;
import cn.oyzh.easyshell.llm.session.LLMSessionContext;
import cn.oyzh.easyshell.llm.session.LLMSessionTab;
import cn.oyzh.easyshell.llm.session.LLMSessionUtil;
import cn.oyzh.easyshell.llm.skill.LLMSkillSession;
import cn.oyzh.easyshell.llm.skill.LLMSkillScriptMode;
import cn.oyzh.easyshell.llm.tool.LLMToolAccessMode;
import cn.oyzh.easyshell.llm.tool.LLMToolAudit;
import cn.oyzh.easyshell.llm.tool.LLMToolNameStrategy;
import cn.oyzh.easyshell.llm.tool.LLMToolRegistry;
import cn.oyzh.easyshell.llm.tool.LLMToolRuntimePolicy;
import cn.oyzh.easyshell.llm.tool.LLMToolSelection;
import cn.oyzh.easyshell.llm.tool.LLMToolScope;
import cn.oyzh.easyshell.llm.tool.LLMTools;
import cn.oyzh.easyshell.local.ShellLocalExecutionLLMTools;
import cn.oyzh.easyshell.store.llm.LLMMcpServerStore;
import cn.oyzh.easyshell.store.llm.LLMPromptStore;
import cn.oyzh.easyshell.store.llm.LLMSessionStore;
import cn.oyzh.easyshell.tabs.ShellConnectTab;
import cn.oyzh.easyshell.tabs.ShellTabPane;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.fx.plus.controller.SubStageController;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.i18n.I18nResourceBundle;
import com.alibaba.fastjson2.JSONObject;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.memory.ChatMemoryService;
import dev.langchain4j.service.tool.ToolArgumentsErrorHandler;
import dev.langchain4j.service.tool.ToolExecution;
import dev.langchain4j.service.tool.ToolProvider;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


/**
 * 聊天业务
 *
 * @author oyzh
 * @since 2025-03-03
 */
public class ShellChatController extends SubStageController {

    @FXML
    private FXVBox root;

    @FXML
    private LLMView view;

    private ShellTabPane tabPane;

    private LLMSession currentSession;

    private final LLMSessionStore sessionStore = LLMSessionStore.INSTANCE;

    private Future<?> sessionSaveTask;

    private final Object sessionSaveLock = new Object();

    private final AtomicLong sessionSaveVersion = new AtomicLong();

    private boolean syncingSessionState;

    @FXML
    private void llm() {
        ShellViewFactory.llmProviders(this.getWindow());
        this.view.refreshProviders();
    }

    @FXML
    private void model() {
        ShellViewFactory.llmModels(this.getWindow());
        this.view.refreshModels();
    }

    @FXML
    private void mcp() {
        ShellViewFactory.llmMcpServers(this.getWindow());
    }

    @FXML
    private void prompt() {
        ShellViewFactory.llmPrompts(this.getWindow());
    }

    @FXML
    private void skill() {
        ShellViewFactory.llmSkills(this.getWindow());
    }

    /**
     * 新建会话
     */
    @FXML
    private void newSession() {
        if (this.rejectSessionChangeDuringRequest()) {
            return;
        }
        if (this.view.isMessageEmpty() || MessageBox.confirm(LLMSessionUtil.newSessionConfirm(), this.getWindow())) {
            this.createNewSession();
        }
    }

    /**
     * 会话历史
     */
    @FXML
    private void history() {
        if (this.rejectSessionChangeDuringRequest()) {
            return;
        }
        this.saveCurrentSession();
        StageAdapter adapter = ShellViewFactory.llmSessions(this.getWindow());
        LLMSession session = adapter == null ? null : adapter.getProp("session");
        if (session != null) {
            this.restoreSession(session);
        } else {
            this.syncCurrentSessionAfterHistory();
        }
    }

    @Override
    protected void bindListeners() {
        this.initLLM();
        this.view.setStopHandler(this::stopCurrentRequest);
        this.view.setSendValidator(this::validateRequestForSend);
        this.view.getModelComboBox().selectedItemChanged((observable, oldValue, newValue) -> this.initLLM());
        this.view.getSessionTabComboBox().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (this.activeRequest != null) {
                this.syncingSessionState = true;
                try {
                    if (oldValue == null) {
                        this.view.getSessionTabComboBox().clearSelection();
                    } else {
                        LLMSessionTab previous = this.view.findSessionTab(oldValue.getId());
                        if (previous == null) {
                            this.view.getSessionTabComboBox().clearSelection();
                        } else {
                            this.view.setBoundTab(previous);
                        }
                    }
                } finally {
                    this.syncingSessionState = false;
                }
                return;
            }
            if (!this.syncingSessionState) {
                this.updateCurrentSessionTab(newValue);
            }
            this.initLLM();
        });
        this.view.getMessageListView().setMessageUpdateHandler(this::saveCurrentSession);
        this.view.getThinkingLevelComboBox().selectedItemChanged((observable, oldValue, newValue) -> this.initLLM());
        this.root.visibleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                if (this.currentSession == null) {
                    this.loadInitialSession();
                }
            } else {
                this.saveCurrentSession();
            }
        });
        super.bindListeners();
    }

    private LLMAssistant assistant;

    private volatile LLMRequest activeRequest;

    private static final class LLMRequest {

        private final AtomicBoolean cancelled = new AtomicBoolean();

        private final AtomicBoolean finished = new AtomicBoolean();

        private final AtomicBoolean processing = new AtomicBoolean(true);

        private volatile Thread thread;

        private volatile LLMMessage placeholder;

        private volatile List<LLMTools> tools;

        private volatile LLMMcpToolSession mcpSession;

        private volatile LLMSkillSession skillSession;

        private LLMSkillScriptMode skillScriptMode;

        private LLMAgentRuntime agentRuntime;

        private List<LLMMcpServer> mcpServers;

        private LLMPrompt prompt;

        private LLMProvider provider;

        private LLMThinkingLevel thinkingLevel;

        private LLMAgentMode agentMode;

        private LLMAgentOrchestrationMode orchestrationMode;

        private int agentMaxConcurrency;

        private int agentMaxSteps;

        private LLMAgentFailurePolicy agentFailurePolicy;

        private LLMSessionTab sessionTab;

        private LLMTools sessionTools;

        private LLMToolAccessMode toolAccessMode;

        private LLMExecutionMode executionMode;

        private LLMToolRuntimePolicy toolPolicy;

        private volatile LLMToolAudit audit;

        private String userText;

        private String model;

        private int maxOutputTokens;

        private int maxMessages;

        private int contextTokenBudget;

        private int timeoutMinutes;

        private boolean streaming;

        private ChatMemory chatMemory;

        private String sessionId;

        private String sessionTabId;

        private LLMModelCapability capabilities;

        private final LLMTokenUsageTracker usageTracker = new LLMTokenUsageTracker();

        private final AtomicBoolean usageApplied = new AtomicBoolean();

        private long startedAtMillis;
    }

    private LLMAssistant initAssistant(LLMRequest request) {
        LLMProvider provider = request.provider;
        String modelName = LLMUtil.resolveModel(provider, request.model);
        request.model = modelName;
        boolean toolsEnabled = request.capabilities == null
                || request.capabilities.isTools();
        // 服务
        AiServices<LLMAssistant> services = AiServices.builder(LLMAssistant.class);
        // 当前 Tab 是默认操作目标，Local Tab 同样属于 SESSION。
        ShellLLMTools internalTools = request.sessionTools == null
                || request.toolAccessMode == LLMToolAccessMode.COMPLETE
                ? new ShellLLMTools(this.tabPane)
                : null;
        // 本机执行能力独立保留，不随 SESSION 消失。
        ShellLocalExecutionLLMTools localExecutionTools = new ShellLocalExecutionLLMTools();
        List<LLMTools> createdTools = new ArrayList<>(2);
        if (request.sessionTools != null) {
            createdTools.add(request.sessionTools);
        }
        if (internalTools != null) {
            createdTools.add(internalTools);
        }
        createdTools.add(localExecutionTools);
        LLMToolSelection toolSelection = new LLMToolSelection(
                request.sessionTools, internalTools, localExecutionTools,
                request.executionMode, request.toolAccessMode);
        String promptSystemMessage = LLMPromptUtil.systemMessage(request.prompt);
        LLMSessionContext sessionContext = LLMSessionContext.from(request.sessionTab);
        List<LLMTools> tools = toolsEnabled
                ? new ArrayList<>(toolSelection.tools())
                : List.of();
        if (!toolsEnabled) {
            this.closeTools(createdTools);
        }
        request.tools = tools;
        // MCP 工具，单个 Server 失败不会影响其他工具
        request.mcpSession = toolsEnabled
                ? LLMMcpToolSession.connect(
                        request.mcpServers,
                        progress -> this.updateMcpProgress(request, progress))
                : LLMMcpToolSession.connect(List.of());
        request.skillSession = toolsEnabled
                ? LLMSkillSession.loadEnabled(request.skillScriptMode)
                : LLMSkillSession.emptySession();
        List<ToolProvider> toolProviders = new ArrayList<>(2);
        if (request.mcpSession.hasClients()) {
            toolProviders.add(request.mcpSession.getToolProvider());
        }
        if (request.skillSession.hasSkills()) {
            toolProviders.addAll(request.skillSession.getToolProviders());
        }
        Map<String, LLMToolScope> toolScopes = LLMToolRegistry.scopes(tools);
        for (String name : request.mcpSession.getToolNames()) {
            toolScopes.put(name, LLMToolScope.MCP);
        }
        for (String name : request.skillSession.getToolNames()) {
            toolScopes.put(name, LLMToolScope.SKILL);
        }
        request.toolPolicy = new LLMToolRuntimePolicy(
                request.executionMode, request.toolAccessMode,
                sessionContext, request.userText, toolScopes);
        List<LLMTools> exposedTools = tools.stream()
                .filter(tool -> request.toolPolicy.exposes(tool.scope()))
                .toList();
        LLMToolRegistry.configure(services, tools, toolProviders, request.toolPolicy);
        for (String skillError : request.skillSession.getErrors()) {
            JulLog.warn("llm skill unavailable: " + skillError);
        }
        if (!request.mcpSession.getErrors().isEmpty()) {
            String error = String.format(I18nResourceBundle.i18nString("shell.llm.mcp.testFail"),
                    String.join("; ", request.mcpSession.getErrors()));
            this.view.addErrorMessage(error, request.sessionTabId);
        }
        LLMExecutionMode executionMode = request.executionMode;
        // 工具相关配置
        services.maxToolCallingRoundTrips(100);
        services.toolArgumentsErrorHandler(ToolArgumentsErrorHandler.sendExceptionMessageToLlm());
        Set<String> dynamicToolNames = new LinkedHashSet<>();
        if (request.toolPolicy.exposes(LLMToolScope.MCP)) {
            dynamicToolNames.addAll(request.mcpSession.getToolNames());
        }
        if (request.toolPolicy.exposes(LLMToolScope.SKILL)) {
            dynamicToolNames.addAll(request.skillSession.getToolNames());
        }
        // 工具调用审计
        LLMToolAudit audit = new LLMToolAudit(tools,
                request.sessionId, request.sessionTabId,
                provider.getName(), modelName, executionMode);
        request.audit = audit;
        audit.registerToolAccessMode(request.toolAccessMode);
        if (request.sessionTab != null) {
            audit.registerSessionContext(request.sessionTab.getType(),
                    request.sessionTab.getHost(), request.sessionTab.getUser());
        }
        audit.registerToolSources(request.mcpSession.getToolSources());
        audit.registerToolSources(request.skillSession.getToolSources(), LLMToolScope.SKILL);
        services.hallucinatedToolNameStrategy(LLMToolRegistry.protectHallucinatedStrategy(
                LLMToolNameStrategy.create(exposedTools, dynamicToolNames),
                request.toolPolicy, audit));
        services.afterToolExecution(audit::record);
        String systemMessage = joinSystemMessages(
                promptSystemMessage,
                sessionContext == null ? "" : sessionContext.promptText(),
                toolsEnabled && request.toolPolicy.exposes(LLMToolScope.SESSION)
                        ? toolSelection.sessionSystemMessage() : "",
                toolsEnabled && request.toolPolicy.exposes(LLMToolScope.INTERNAL)
                        ? toolSelection.internalSystemMessage() : "",
                toolsEnabled && request.toolPolicy.exposes(LLMToolScope.LOCAL_EXECUTION)
                        ? toolSelection.localExecutionSystemMessage() : "",
                toolsEnabled ? "" : """
                        ## Model capability
                        Tool calling is disabled for the selected model. Do not claim to call or execute tools.
                        """,
                request.toolPolicy.promptText());
        if (request.mcpSession.hasClients()
                && request.toolPolicy.exposes(LLMToolScope.MCP)) {
            systemMessage = joinSystemMessages(systemMessage, request.mcpSession.getSystemMessage());
        }
        if (request.skillSession.hasSkills()
                && request.toolPolicy.exposes(LLMToolScope.SKILL)) {
            systemMessage = joinSystemMessages(systemMessage,
                    request.skillSession.getSystemMessage());
        }
        services.systemMessage(systemMessage);
        // 使用当前会话历史初始化内存，当前消息由大模型服务负责追加
        services.chatMemory(request.chatMemory);
        if (request.streaming) {
            // 创建模型
            StreamingChatModel model = request.usageTracker.wrap(
                    LLMUtil.createStreamingChatModel(provider,
                    modelName, request.thinkingLevel, request.maxOutputTokens,
                    LLMRequestTimeout.fromMinutes(request.timeoutMinutes).getDuration()));
            ChatModel agentModel = request.usageTracker.wrap(LLMUtil.createChatModel(provider,
                    modelName, request.thinkingLevel, request.maxOutputTokens,
                    LLMRequestTimeout.fromMinutes(request.timeoutMinutes).getDuration()));
            services.streamingChatModel(model);
            // 创建工具
            this.assistant = services.build();
            request.agentRuntime = this.createAgentRuntime(agentModel, model, tools,
                    request.mcpSession, toolSelection.sessionSystemMessage(),
                    toolSelection.internalSystemMessage(),
                    toolSelection.localExecutionSystemMessage(), promptSystemMessage,
                    request.skillSession,
                    sessionContext,
                    executionMode, audit, request.maxMessages, request.agentMode,
                    request.orchestrationMode, request.agentMaxConcurrency,
                    request.agentMaxSteps, request.agentFailurePolicy);
            request.agentRuntime.setToolPolicy(request.toolPolicy);
            request.agentRuntime.setContextTokenBudget(request.contextTokenBudget);
        } else {
            // 创建模型
            ChatModel model = request.usageTracker.wrap(LLMUtil.createChatModel(provider,
                    modelName, request.thinkingLevel, request.maxOutputTokens,
                    LLMRequestTimeout.fromMinutes(request.timeoutMinutes).getDuration()));
            services.chatModel(model);
            // 创建工具
            this.assistant = services.build();
            request.agentRuntime = this.createAgentRuntime(model, null, tools,
                    request.mcpSession, toolSelection.sessionSystemMessage(),
                    toolSelection.internalSystemMessage(),
                    toolSelection.localExecutionSystemMessage(), promptSystemMessage,
                    request.skillSession,
                    sessionContext,
                    executionMode, audit, request.maxMessages, request.agentMode,
                    request.orchestrationMode, request.agentMaxConcurrency,
                    request.agentMaxSteps, request.agentFailurePolicy);
            request.agentRuntime.setToolPolicy(request.toolPolicy);
            request.agentRuntime.setContextTokenBudget(request.contextTokenBudget);
        }
        return this.assistant;
    }

    private LLMAgentRuntime createAgentRuntime(ChatModel chatModel,
                                               StreamingChatModel streamingChatModel,
                                               List<LLMTools> tools,
                                               LLMMcpToolSession mcpSession,
                                               String sessionSystemMessage,
                                               String internalSystemMessage,
                                               String localExecutionSystemMessage,
                                               String promptSystemMessage,
                                               LLMSkillSession skillSession,
                                               LLMSessionContext sessionContext,
                                               LLMExecutionMode executionMode,
                                               LLMToolAudit audit,
                                               int maxMessages,
                                               LLMAgentMode agentMode,
                                               LLMAgentOrchestrationMode orchestrationMode,
                                               int agentMaxConcurrency,
                                               int agentMaxSteps,
                                               LLMAgentFailurePolicy agentFailurePolicy) {
        String mcpSystemMessage = mcpSession == null ? "" : mcpSession.getSystemMessage();
        LLMAgentRuntime runtime = new LLMAgentRuntime(chatModel, streamingChatModel, tools, mcpSession,
                sessionSystemMessage, internalSystemMessage, localExecutionSystemMessage,
                sessionContext, mcpSystemMessage,
                tools.isEmpty() ? "" : LLMUtil.executionSystemMessage(executionMode),
                LLMUtil.languageSystemMessage(), audit, maxMessages, agentMode,
                orchestrationMode, agentMaxConcurrency, agentMaxSteps, agentFailurePolicy);
        runtime.setPromptSystemMessage(promptSystemMessage);
        runtime.setSkillSession(skillSession);
        return runtime;
    }

    private static String joinSystemMessages(String... messages) {
        return LLMUtil.joinSystemMessages(messages);
    }

    private ChatMemory buildChatMemory(LLMMessage currentMessage,
                                       LLMRequest request) {
        String memoryId = this.currentSession == null ? ChatMemoryService.DEFAULT : this.currentSession.getUid();
        LLMTokenAwareChatMemory memory = LLMTokenAwareChatMemory.builder()
                .id(memoryId)
                .maxMessages(this.view.getLlmMaxMessages())
                .maxTokens(request.contextTokenBudget)
                .build();
        List<ChatMessage> history = new ArrayList<>();
        for (LLMMessage item : this.view.getMessages()) {
            if (item == null || item.isError()) {
                continue;
            }
            if (currentMessage != null && Objects.equals(item.getMessageId(), currentMessage.getMessageId())) {
                continue;
            }
            String text = item.getMessage();
            if (text == null || text.isBlank()) {
                continue;
            }
            history.add(item.isUser() ? UserMessage.from(text) : AiMessage.from(text));
        }
        memory.set(history);
        return LLMSafeChatMemory.wrap(memory);
    }

    /**
     * 初始化大模型
     */
    private void initLLM() {
        this.assistant = null;
        this.view.setSendHandler(message -> {
            if (!this.validateRequestForSend()) {
                return;
            }
            LLMProvider provider = this.validateProviderForSend();
            if (provider == null) {
                return;
            }
            LLMRequest request = new LLMRequest();
            request.startedAtMillis = System.currentTimeMillis();
            request.userText = message.getMessage();
            request.provider = provider;
            request.model = this.view.getSelectedModel();
            request.capabilities = this.view.getModelCapability();
            request.maxOutputTokens = this.view.getLlmMaxOutputTokens();
            request.contextTokenBudget = LLMParameterUtil.historyTokenBudget(
                    request.capabilities.getContextWindowTokens(),
                    request.maxOutputTokens);
            request.maxMessages = this.view.getLlmMaxMessages();
            request.timeoutMinutes = this.view.getLlmTimeoutMinutes();
            request.streaming = this.view.isStreamingEnabled();
            request.chatMemory = this.buildChatMemory(message, request);
            request.sessionTab = this.view.getSelectedSession();
            request.thinkingLevel = request.capabilities.isThinking()
                    ? this.view.getSelectedThinkingLevel()
                    : LLMThinkingLevel.OFF;
            request.agentMode = this.view.getSelectedAgentMode();
            request.orchestrationMode = this.view.getSelectedOrchestrationMode();
            request.agentMaxConcurrency = this.view.getSelectedAgentMaxConcurrency();
            request.agentMaxSteps = this.view.getSelectedAgentMaxSteps();
            request.agentFailurePolicy = this.view.getSelectedAgentFailurePolicy();
            request.executionMode = this.view.getSelectedExecutionMode();
            request.toolAccessMode = this.view.getSelectedToolAccessMode();
            request.skillScriptMode = this.view.getSelectedSkillScriptMode();
            request.prompt = this.loadSelectedPrompt();
            request.sessionId = this.currentSession == null ? null : this.currentSession.getUid();
            request.sessionTabId = request.sessionTab == null ? null : request.sessionTab.getId();
            if (request.sessionTab != null && request.sessionTab.getToolsFunc() != null) {
                request.sessionTools = request.sessionTab.getToolsFunc().get();
            }
            this.activeRequest = request;
            this.view.setBusy(true);
            request.mcpServers = this.loadEnabledMcpServers();
            if (!request.mcpServers.isEmpty()) {
                request.placeholder = this.view.addAssistantMessage(String.format(
                        I18nResourceBundle.i18nString("shell.llm.mcp.progress.start"),
                        request.mcpServers.size()), request.sessionTabId);
            }
            // 异步执行
            request.thread = ThreadUtil.startVirtual(() -> this.prepareAndExecuteRequest(request, message));
        });
    }

    private List<LLMMcpServer> loadEnabledMcpServers() {
        try {
            return LLMMcpServerStore.INSTANCE.selectEnabled();
        } catch (Exception ex) {
            JulLog.warn("load enabled mcp servers failed", ex);
            return List.of();
        }
    }

    private LLMPrompt loadSelectedPrompt() {
        try {
            return LLMPromptStore.INSTANCE.selectByUid(this.view.getSelectedPromptUid());
        } catch (Exception ex) {
            JulLog.warn("load selected llm prompt failed", ex);
            return null;
        }
    }

    private void updateMcpProgress(LLMRequest request, LLMMcpToolSession.Progress progress) {
        if (!this.isRequestActive(request) || request.placeholder == null) {
            return;
        }
        String text;
        if (progress.completed() == 0) {
            text = String.format(I18nResourceBundle.i18nString("shell.llm.mcp.progress.start"),
                    progress.total());
        } else if (progress.error() != null) {
            text = String.format(I18nResourceBundle.i18nString("shell.llm.mcp.progress.failed"),
                    progress.serverName(), progress.error());
        } else {
            text = String.format(I18nResourceBundle.i18nString("shell.llm.mcp.progress.connected"),
                    progress.completed(), progress.total(), progress.totalTools());
        }
        this.view.replaceMessage(request.placeholder, text);
    }

    private LLMProvider validateProviderForSend() {
        if (this.view.getProviderComboBox().isItemEmpty()) {
            ShellViewFactory.llmProviders(this.getWindow());
            this.view.refreshProviders();
            return null;
        }
        LLMProvider provider = this.view.getSelectedProvider();
        if (provider == null) {
            MessageBox.warn(I18nHelper.pleaseSelectProvider(), this.getWindow());
        }
        return provider;
    }

    private void prepareAndExecuteRequest(LLMRequest request, LLMMessage message) {
        request.thread = Thread.currentThread();
        try {
            if (!this.isRequestActive(request)) {
                this.closeRequestResources(request);
                return;
            }
            LLMAssistant assistant = this.initAssistant(request);
            if (!this.isRequestActive(request)) {
                this.closeRequestResources(request);
                return;
            }
            this.executeRequest(request, assistant, message, request.streaming);
        } catch (Exception ex) {
            if (this.isRequestActive(request)) {
                JulLog.error("llm init error", ex);
                String error = this.llmErrorMessage(ex, request.placeholder);
                this.clearPlaceholder(request);
                this.view.addErrorMessage(error, message.getSessionTabId());
                this.finishRequest(request, true);
            } else {
                this.closeRequestResources(request);
            }
        } finally {
            request.processing.set(false);
            this.closeRequestResourcesIfDone(request);
        }
    }

    private void executeRequest(LLMRequest request, LLMAssistant assistant, LLMMessage message, boolean streaming) {
        request.thread = Thread.currentThread();
        try {
            if (!this.isRequestActive(request)) {
                return;
            }
            List<Content> contents;
            try {
                contents = LLMFileContentUtil.toUserContents(message.getMessage(), message.getFile());
            } catch (Exception ex) {
                if (!this.isRequestActive(request)) {
                    return;
                }
                JulLog.error("llm file content error", ex);
                this.clearPlaceholder(request);
                this.view.addErrorMessage(ex.getMessage(), message.getSessionTabId());
                this.finishRequest(request, true);
                return;
            }
            if (!this.isRequestActive(request)) {
                return;
            }
            if (contents.isEmpty()) {
                this.clearPlaceholder(request);
                this.view.addErrorMessage(I18nHelper.invalidFile(), message.getSessionTabId());
                this.finishRequest(request, true);
                return;
            }
            LLMMessage placeholder = request.placeholder;
            if (placeholder == null) {
                placeholder = this.view.addAssistantMessage(
                        ShellI18nHelper.thinkingPlaceholder(), message.getSessionTabId());
            } else {
                this.view.replaceMessage(placeholder, ShellI18nHelper.thinkingPlaceholder());
            }
            LLMMessage targetPlaceholder = placeholder;
            request.placeholder = targetPlaceholder;
            if (!this.isRequestActive(request)) {
                this.markStoppedPlaceholder(targetPlaceholder);
                return;
            }
            if (request.agentRuntime != null && message.getFile() == null) {
                this.executeAgentRequest(request, message, targetPlaceholder);
                return;
            }
            if (streaming) {
                this.startAssistantStream(request, assistant, contents,
                        targetPlaceholder, message, 0);
            } else {
                try {
                    ChatResponse response = LLMNetworkRetry.execute("llm chat",
                            () -> assistant.chatResponse(contents));
                    if (!this.isRequestActive(request)) {
                        return;
                    }
                    String thinking = LLMResponseUtil.finalThinking(response);
                    String finalText = LLMResponseUtil.finalText(response);
                    placeholder.finishThinking();
                    this.view.replaceThinking(placeholder, thinking == null ? "" : thinking);
                    this.view.replaceMessage(placeholder, finalText == null ? "" : finalText);
                    if (finalText == null) {
                        this.view.addErrorMessage(ShellI18nHelper.emptyResponse(), message.getSessionTabId());
                    }
                    if (LLMResponseUtil.outputTruncated(response)) {
                        this.view.addErrorMessage(ShellI18nHelper.outputTruncated(), message.getSessionTabId());
                    }
                    this.completeRequestUsage(request, response);
                    this.finishRequest(request, true);
                } catch (Exception ex) {
                    if (!this.isRequestActive(request)) {
                        return;
                    }
                    JulLog.error("llm error", ex);
                    placeholder.finishThinking();
                    this.view.addErrorMessage(this.llmErrorMessage(ex, placeholder),
                            message.getSessionTabId());
                    this.finishRequest(request, true);
                }
            }
        } catch (Exception ex) {
            if (!this.isRequestActive(request)) {
                return;
            }
            JulLog.error("llm request error", ex);
            this.view.addErrorMessage(this.llmErrorMessage(ex, request.placeholder),
                    message.getSessionTabId());
            this.finishRequest(request, true);
        }
    }

    private void startAssistantStream(LLMRequest request,
                                      LLMAssistant assistant,
                                      List<Content> contents,
                                      LLMMessage placeholder,
                                      LLMMessage message,
                                      int retry) {
        AtomicBoolean firstResponse = new AtomicBoolean(true);
        AtomicBoolean firstThinking = new AtomicBoolean(true);
        AtomicBoolean settled = new AtomicBoolean();
        assistant.chatStream(contents)
                .onPartialThinking(token -> {
                    if (!this.isRequestActive(request) || StringUtil.isBlank(token.text())) {
                        return;
                    }
                    if (firstThinking.getAndSet(false)
                            && view.isShowThinking()
                            && Objects.equals(placeholder.getMessage(), ShellI18nHelper.thinkingPlaceholder())) {
                        this.view.replaceMessage(placeholder, "");
                    }
                    this.view.appendThinking(placeholder, token.text());
                })
                .onPartialResponse(token -> {
                    if (!this.isRequestActive(request)) {
                        return;
                    }
                    if (firstResponse.getAndSet(false)) {
                        placeholder.finishThinking();
                        this.view.replaceMessage(placeholder, token);
                    } else {
                        this.view.appendMessage(placeholder, token);
                    }
                })
                .onError(ex -> {
                    if (!this.isRequestActive(request)
                            || !settled.compareAndSet(false, true)) {
                        return;
                    }
                    if (retry < LLMNetworkRetry.MAX_RETRIES
                            && LLMNetworkRetry.isRetryable(ex)) {
                        int nextRetry = retry + 1;
                        JulLog.warn("llm stream interrupted, retrying "
                                + nextRetry + "/" + LLMNetworkRetry.MAX_RETRIES, ex);
                        this.view.replaceThinking(placeholder, "");
                        this.view.replaceMessage(placeholder, ShellI18nHelper.thinkingPlaceholder());
                        try {
                            Thread.sleep(LLMNetworkRetry.retryDelayMillis(nextRetry));
                        } catch (InterruptedException interrupted) {
                            Thread.currentThread().interrupt();
                            if (this.isRequestActive(request)) {
                                this.view.addErrorMessage(
                                        ShellI18nHelper.streamRetryFailed(),
                                        message.getSessionTabId());
                                this.finishRequest(request, true);
                            }
                            return;
                        }
                        if (this.isRequestActive(request)) {
                            this.startAssistantStream(request, assistant, contents,
                                    placeholder, message, nextRetry);
                        }
                        return;
                    }
                    JulLog.error("llm stream error", ex);
                    placeholder.finishThinking();
                    this.view.flushMessageUpdates();
                    this.view.addErrorMessage(
                            this.llmErrorMessage(ex, placeholder),
                            message.getSessionTabId());
                    this.finishRequest(request, true);
                })
                .onCompleteResponse(response -> {
                    if (settled.compareAndSet(false, true)) {
                        this.completeStreamingRequest(request, placeholder, response);
                    }
                })
                .start();
    }

    private void executeAgentRequest(LLMRequest request,
                                     LLMMessage message,
                                     LLMMessage placeholder) {
        AtomicBoolean firstResponse = new AtomicBoolean(true);
        AtomicBoolean firstThinking = new AtomicBoolean(true);
        ConcurrentHashMap<String, LLMAgentTrace> traces = new ConcurrentHashMap<>();
        AtomicInteger commandIndex = new AtomicInteger();
        if (request.audit != null) {
            request.audit.setExecutionListener(execution -> this.recordLocalCommandTrace(
                    request, placeholder, traces, commandIndex, execution));
        }
        try {
            this.view.replaceAgentTrace(placeholder, "");
            ChatResponse response = request.agentRuntime.execute(
                    message.getMessage(),
                    request.chatMemory.messages(),
                    new LLMAgentListener() {
                        @Override
                        public void onEvent(LLMAgentEvent event) {
                            if (!isRequestActive(request) || event == null
                                    || event.type() == LLMAgentEventType.COMPLETED
                                    || !firstResponse.get()) {
                                return;
                            }
                            // Agent 事件只用于正式回复开始前的状态展示。正文开始流式输出后，
                            // 后续事件不能再覆盖或混入正文。
                            view.replaceMessage(placeholder, agentEventText(event));
                        }

                        @Override
                        public void onPartialThinking(String text) {
                            if (!isRequestActive(request) || StringUtil.isBlank(text)) {
                                return;
                            }
                            if (firstThinking.getAndSet(false)
                                    && firstResponse.get()
                                    && view.isShowThinking()) {
                                view.replaceMessage(placeholder, "");
                            }
                            view.appendThinking(placeholder, text);
                        }

                        @Override
                        public void onPartialResponse(String text) {
                            if (!isRequestActive(request) || text == null || text.isEmpty()) {
                                return;
                            }
                            if (firstResponse.getAndSet(false)) {
                                placeholder.finishThinking();
                                view.replaceMessage(placeholder, text);
                            } else {
                                view.appendMessage(placeholder, text);
                            }
                        }

                        @Override
                        public void onAgentTrace(LLMAgentTrace trace) {
                            if (!isRequestActive(request) || trace == null) {
                                return;
                            }
                            synchronized (traces) {
                                traces.put(trace.id(), trace);
                                view.replaceAgentTrace(placeholder,
                                        LLMAgentTraceFormatter.format(traces.values()));
                            }
                        }
                    },
                    request.cancelled::get);
            if (!this.isRequestActive(request)) {
                return;
            }
            if (firstResponse.get() && LLMResponseUtil.finalText(response) == null) {
                this.view.replaceMessage(placeholder, "");
            }
            this.completeStreamingRequest(request, placeholder, response);
        } catch (Exception ex) {
            if (!this.isRequestActive(request)) {
                return;
            }
            JulLog.error("llm agent error", ex);
            placeholder.finishThinking();
            this.view.addErrorMessage(this.llmErrorMessage(ex, placeholder),
                    message.getSessionTabId());
            this.finishRequest(request, true);
        } finally {
            if (request.audit != null) {
                request.audit.setExecutionListener(null);
            }
        }
    }

    private void recordLocalCommandTrace(LLMRequest request,
                                         LLMMessage placeholder,
                                         ConcurrentHashMap<String, LLMAgentTrace> traces,
                                         AtomicInteger commandIndex,
                                         ToolExecution execution) {
        if (!this.isRequestActive(request) || request.audit == null
                || !request.audit.isLocalCommand(execution)) {
            return;
        }
        String command = this.commandArgument(execution, "command");
        if (StringUtil.isBlank(command)) {
            return;
        }
        String workingDirectory = this.commandArgument(execution, "workingDirectory");
        String result = this.toolResult(execution);
        boolean failed = execution.hasFailed() || commandFailed(result);
        long startedAt = execution.startTime() == null
                ? System.currentTimeMillis()
                : execution.startTime().atZone(java.time.ZoneId.systemDefault())
                        .toInstant().toEpochMilli();
        long finishedAt = execution.finishTime() == null
                ? System.currentTimeMillis()
                : execution.finishTime().atZone(java.time.ZoneId.systemDefault())
                        .toInstant().toEpochMilli();
        LLMAgentTrace trace = new LLMAgentTrace(
                "tool-" + commandIndex.incrementAndGet(),
                LLMAgentTrace.Type.TOOL,
                0, 0,
                execution.request().name(),
                command,
                workingDirectory == null ? "" : workingDirectory,
                failed ? "" : result,
                failed ? result : "",
                failed ? LLMAgentTrace.Status.FAILED : LLMAgentTrace.Status.COMPLETED,
                startedAt, finishedAt,
                LLMAgentOrchestrationMode.SEQUENTIAL);
        synchronized (traces) {
            traces.put(trace.id(), trace);
            this.view.replaceAgentTrace(placeholder,
                    LLMAgentTraceFormatter.format(traces.values()));
        }
    }

    private String commandArgument(ToolExecution execution, String name) {
        try {
            String arguments = execution.request() == null
                    ? null
                    : execution.request().arguments();
            if (StringUtil.isBlank(arguments)) {
                return null;
            }
            JSONObject object = JSONUtil.parseObject(arguments);
            return object == null ? null : object.getString(name);
        } catch (Exception ex) {
            return null;
        }
    }

    private String toolResult(ToolExecution execution) {
        try {
            String result = execution.result();
            return result == null ? "" : result;
        } catch (Exception ex) {
            return execution.hasFailed() ? String.valueOf(ex.getMessage()) : "";
        }
    }

    private static boolean commandFailed(String result) {
        if (result == null || result.isBlank()) {
            return false;
        }
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("(?m)^exitCode:\\s*(-?\\d+)")
                .matcher(result);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1)) != 0;
            } catch (NumberFormatException ignored) {
                return true;
            }
        }
        return result.contains("timedOut: true") || result.contains("interrupted: true");
    }

    private String llmErrorMessage(Throwable ex, LLMMessage placeholder) {
        if (LLMNetworkRetry.isRetryable(ex)) {
            String text = placeholder == null ? null : placeholder.getMessage();
            if (StringUtil.isBlank(text)
                    || Objects.equals(text, ShellI18nHelper.thinkingPlaceholder())) {
                return ShellI18nHelper.streamRetryFailed();
            }
            return ShellI18nHelper.streamInterrupted();
        }
        String message = ex == null ? null : ex.getMessage();
        return StringUtil.isBlank(message) ? I18nHelper.unknown() : message;
    }

    private String agentEventText(LLMAgentEvent event) {
        return switch (event.type()) {
            case PLANNING -> I18nResourceBundle.i18nString("shell.llm.agent.planning");
            case STEP_STARTED -> String.format(
                    I18nResourceBundle.i18nString("shell.llm.agent.step"),
                    event.stepIndex() + 1, event.totalSteps(), event.objective());
            case STEP_COMPLETED -> String.format(
                    I18nResourceBundle.i18nString("shell.llm.agent.stepCompleted"),
                    event.stepIndex() + 1, event.totalSteps());
            case SYNTHESIZING -> I18nResourceBundle.i18nString("shell.llm.agent.synthesizing");
            case COMPLETED -> ShellI18nHelper.thinkingPlaceholder();
        };
    }

    private void completeStreamingRequest(LLMRequest request, LLMMessage placeholder, ChatResponse response) {
        try {
            if (!this.isRequestActive(request)) {
                return;
            }
            this.completeRequestUsage(request, response);
            placeholder.finishThinking();
            this.view.flushMessageUpdates();
            String finalText = LLMResponseUtil.finalText(response);
            String mergedThinking = LLMResponseUtil.mergeThinking(
                    placeholder.getThinking(), LLMResponseUtil.finalThinking(response));
            if (!Objects.equals(mergedThinking, placeholder.getThinking())) {
                this.view.replaceThinking(placeholder, mergedThinking);
            }
            if (finalText != null) {
                this.view.replaceMessage(placeholder, finalText);
            } else {
                String partialText = placeholder.getMessage();
                if (partialText == null || partialText.isBlank()
                        || Objects.equals(partialText, ShellI18nHelper.thinkingPlaceholder())) {
                    this.view.replaceMessage(placeholder, "");
                    this.view.addErrorMessage(ShellI18nHelper.emptyResponse(), placeholder.getSessionTabId());
                }
            }
            if (LLMResponseUtil.outputTruncated(response)) {
                this.view.addErrorMessage(ShellI18nHelper.outputTruncated(), placeholder.getSessionTabId());
            }
        } finally {
            this.finishRequest(request, true);
        }
    }

    private boolean isRequestActive(LLMRequest request) {
        return request != null
                && request == this.activeRequest
                && !request.cancelled.get()
                && !request.finished.get();
    }

    private void stopCurrentRequest() {
        LLMRequest request = this.activeRequest;
        if (request == null || !request.cancelled.compareAndSet(false, true)) {
            this.view.setBusy(false);
            return;
        }
        ThreadUtil.interrupt(request.thread);
        this.markStoppedPlaceholder(request.placeholder);
        this.finishRequest(request, true);
    }

    private void markStoppedPlaceholder(LLMMessage message) {
        if (message == null) {
            return;
        }
        message.finishThinking();
        this.view.flushMessageUpdates();
        String text = message.getMessage();
        if (text == null || text.isBlank() || Objects.equals(text, ShellI18nHelper.thinkingPlaceholder())) {
            this.view.replaceMessage(message, I18nHelper.stopped());
        }
    }

    private void clearPlaceholder(LLMRequest request) {
        if (request == null) {
            return;
        }
        LLMMessage placeholder = request.placeholder;
        request.placeholder = null;
        if (placeholder != null) {
            this.view.replaceMessage(placeholder, "");
        }
    }

    private void finishRequest(LLMRequest request, boolean save) {
        if (request == null) {
            return;
        }
        if (request.finished.compareAndSet(false, true)) {
            if (this.activeRequest == request) {
                this.activeRequest = null;
            }
            this.applyRequestUsage(request);
            this.view.setBusy(false);
            if (save) {
                this.saveCurrentSession();
            }
        }
        this.closeRequestResourcesIfDone(request);
    }

    private void completeRequestUsage(LLMRequest request, ChatResponse response) {
        if (request != null) {
            request.usageTracker.complete(
                    response == null ? null : response.tokenUsage(),
                    requestDurationMillis(request));
        }
    }

    private void applyRequestUsage(LLMRequest request) {
        if (request == null || !request.usageApplied.compareAndSet(false, true)) {
            return;
        }
        LLMTokenUsageStats stats = request.usageTracker.snapshot(
                requestDurationMillis(request));
        LLMSession session = this.currentSession;
        if (session != null && Objects.equals(session.getUid(), request.sessionId)) {
            session.setTokenUsageStats(stats);
            this.view.setTokenUsageStats(stats);
        }
    }

    private static long requestDurationMillis(LLMRequest request) {
        return request.startedAtMillis <= 0
                ? LLMTokenUsageStats.UNKNOWN
                : Math.max(0, System.currentTimeMillis() - request.startedAtMillis);
    }

    private void closeRequestResourcesIfDone(LLMRequest request) {
        if (request != null && request.finished.get() && !request.processing.get()) {
            this.closeRequestResources(request);
        }
    }

    private synchronized void closeRequestResources(LLMRequest request) {
        List<LLMTools> requestTools = request.tools;
        request.tools = null;
        if (requestTools != null) {
            this.closeTools(requestTools);
        }
        LLMMcpToolSession mcpSession = request.mcpSession;
        request.mcpSession = null;
        if (mcpSession != null) {
            mcpSession.close();
        }
        LLMSkillSession skillSession = request.skillSession;
        request.skillSession = null;
        if (skillSession != null) {
            skillSession.close();
        }
    }

    private void closeTools(List<LLMTools> tools) {
        if (tools == null) {
            return;
        }
        for (LLMTools tool : tools) {
            try {
                tool.close();
            } catch (Exception ex) {
                JulLog.warn("close llm tools failed", ex);
            }
        }
    }

    @Override
    public void onStageInitialize(StageAdapter stage) {
        super.onStageInitialize(stage);
        if (this.getParent() instanceof ShellMainController mainController) {
            this.tabPane = mainController.getTabPane();
            this.init();
        }
    }

    @Override
    public void destroy() {
        LLMMcpToolSession.closeAllConnections();
        super.destroy();
    }

    /**
     * 初始化
     */
    private void init() {
        this.tabPane.getTabs().addListener((ListChangeListener<Tab>) c -> {
            if (!c.next()) {
                return;
            }
            this.syncingSessionState = true;
            try {
                if (c.wasAdded() || c.wasReplaced()) {
                    for (Tab tab : c.getAddedSubList()) {
                        if (tab instanceof LLMTabCapability capability) {
                            this.addSessionTab(capability);
                        }
                    }
                }
                if (c.wasRemoved() || c.wasReplaced()) {
                    for (Tab tab : c.getRemoved()) {
                        if (tab instanceof LLMTabCapability capability) {
                            this.view.getSessionTabComboBox().removeTab(capability.getTabId());
                            if (this.currentSession != null && Objects.equals(this.currentSession.getSessionTabId(), capability.getTabId())) {
                                this.view.getSessionTabComboBox().clearSelection();
                            }
                        }
                    }
                }
            } finally {
                this.syncingSessionState = false;
            }
        });
        this.syncingSessionState = true;
        try {
            for (Tab tab : this.tabPane.getTabs()) {
                if (tab instanceof LLMTabCapability capability) {
                    this.addSessionTab(capability);
                }
            }
            this.selectCurrentMainTab();
        } finally {
            this.syncingSessionState = false;
        }
        this.loadInitialSession();
    }

    private void addSessionTab(LLMTabCapability capability) {
        LLMSessionTab sessionTab = new LLMSessionTab();
        sessionTab.setId(capability.getTabId());
        sessionTab.setName(capability.getTabName());
        sessionTab.setToolsFunc(capability.getToolsFunc());
        this.copySessionContext(capability, sessionTab);
        this.view.getSessionTabComboBox().addItem(sessionTab);
    }

    private void selectCurrentMainTab() {
        if (this.tabPane == null) {
            return;
        }
        Tab selected = this.tabPane.getSelectionModel().getSelectedItem();
        if (selected instanceof LLMTabCapability capability) {
            this.view.getSessionTabComboBox().selectTab(capability.getTabId());
        } else if (this.view.getSessionTabComboBox().getSelectedIndex() < 0
                && !this.view.getSessionTabComboBox().isItemEmpty()) {
            this.view.getSessionTabComboBox().selectFirst();
        }
    }

    private void loadInitialSession() {
        try {
            List<LLMSession> sessions = this.sessionStore.loadRecent();
            if (!sessions.isEmpty()) {
                this.restoreSession(sessions.getFirst());
                return;
            }
        } catch (Exception ex) {
            JulLog.error("load llm sessions error", ex);
        }
        this.createNewSession(false);
    }

    private void createNewSession() {
        this.createNewSession(true);
    }

    private void createNewSession(boolean flushCurrent) {
        if (flushCurrent) {
            this.saveCurrentSession();
        }
        this.syncingSessionState = true;
        try {
            this.currentSession = LLMSessionUtil.newSession(this.resolveNewSessionTab());
            this.view.clearMessages();
            this.view.setTokenUsageStats(this.currentSession.getTokenUsageStats());
            this.initLLM();
        } finally {
            this.syncingSessionState = false;
        }
    }

    private LLMSessionTab resolveNewSessionTab() {
        if (this.tabPane != null) {
            Tab selected = this.tabPane.getSelectionModel().getSelectedItem();
            if (selected instanceof LLMTabCapability capability) {
                LLMSessionTab sessionTab = this.view.findSessionTab(capability.getTabId());
                if (sessionTab == null) {
                    sessionTab = new LLMSessionTab(capability.getTabId(), capability.getTabName());
                    sessionTab.setToolsFunc(capability.getToolsFunc());
                }
                this.copySessionContext(capability, sessionTab);
                this.view.setBoundTab(sessionTab);
                return sessionTab;
            }
        }
        return this.view.getSelectedSession();
    }

    private void restoreSession(LLMSession session) {
        if (session == null) {
            return;
        }
        this.saveCurrentSession();
        this.syncingSessionState = true;
        try {
            this.currentSession = session;
            this.view.setMessages(LLMSessionUtil.fromJson(session.getMessages()));
            this.view.setTokenUsageStats(session.getTokenUsageStats());
            LLMSessionTab sessionTab = this.view.findSessionTab(session.getSessionTabId());
            if (sessionTab == null) {
                this.view.clearSessionTab();
            } else {
                this.view.setBoundTab(sessionTab);
            }
            this.initLLM();
        } finally {
            this.syncingSessionState = false;
        }
    }

    private void updateCurrentSessionTab(LLMSessionTab sessionTab) {
        if (this.currentSession == null) {
            return;
        }
        String tabId = sessionTab == null ? null : sessionTab.getId();
        if (Objects.equals(this.currentSession.getSessionTabId(), tabId)) {
            return;
        }
        this.currentSession.setSessionTabId(tabId);
        this.saveCurrentSession();
    }

    private boolean rejectSessionChangeDuringRequest() {
        if (this.activeRequest == null) {
            return false;
        }
        MessageBox.warn(I18nResourceBundle.i18nString("shell.llm.requestInProgress"),
                this.getWindow());
        return true;
    }

    private void copySessionContext(LLMTabCapability capability, LLMSessionTab sessionTab) {
        ShellConnect connect = capability instanceof ShellConnectTab connectTab
                ? connectTab.shellConnect()
                : null;
        sessionTab.setType(connect != null
                ? connect.getType()
                : capability.getClass().getSimpleName());
        sessionTab.setHost(connect == null ? null : connect.hostIp());
        int port = connect == null ? -1 : connect.hostPort();
        sessionTab.setPort(port <= 0 ? null : port);
        sessionTab.setUser(connect == null ? null : connect.getUser());
    }

    private boolean validateSessionForSend() {
        if (this.currentSession == null) {
            this.createNewSession(false);
        }
        //String tabId = this.currentSession.getSessionTabId();
        //if (tabId == null || this.tabPane == null) {
        //    return true;
        //}
        //for (Tab tab : this.tabPane.getTabs()) {
        //    if (tab instanceof LLMTabCapability capability && Objects.equals(tabId, capability.getTabId())) {
        //        return true;
        //    }
        //}
        //this.view.addErrorMessage(LLMSessionUtil.sessionExpired());
        //return false;
        return true;
    }

    private boolean validateRequestForSend() {
        if (!this.validateSessionForSend()) {
            return false;
        }
        LLMProvider provider = this.view.getSelectedProvider();
        if (provider == null) {
            return false;
        }
        String model = this.view.getSelectedModel();
        LLMModelCapability capability = this.view.getModelCapability();
        File file = this.view.getSelectedFile();
        if (file != null) {
            LLMFileContentType type = LLMFileContentUtil.contentType(file);
            if (!capability.supports(type)) {
                MessageBox.warn(String.format(
                        I18nResourceBundle.i18nString(
                                "shell.llm.modelCapability.unsupportedAttachment"),
                        model, type.name()), this.getWindow());
                return false;
            }
        }
        LLMContextBudgetUtil.Result budget = LLMContextBudgetUtil.check(
                this.currentSession == null
                        ? LLMTokenUsageStats.EMPTY
                        : this.currentSession.getTokenUsageStats(),
                this.view.getMessages(),
                this.view.getInputArea().getTextTrim(),
                file,
                this.view.getLlmMaxOutputTokens(),
                capability);
        if (!budget.allowed()) {
            MessageBox.warn(String.format(
                    I18nResourceBundle.i18nString(
                            "shell.llm.modelCapability.contextExceeded"),
                    LLMTokenUsageStats.formatTokens(budget.estimatedTokens()),
                    LLMTokenUsageStats.formatTokens(budget.limitTokens())),
                    this.getWindow());
            return false;
        }
        return true;
    }

    private void saveCurrentSession() {
        FXUtil.runLater(() -> {
            // 请求进行中仅更新界面，避免持久化未完成的 LLM 消息
            if (this.activeRequest != null) {
                return;
            }
            LLMSession session = this.currentSession;
            if (session == null) {
                return;
            }
            LLMSessionTab selectedTab = this.view.getSelectedSession();
            if (selectedTab != null && selectedTab.getId() != null && !Objects.equals(session.getSessionTabId(), selectedTab.getId())) {
                session.setSessionTabId(selectedTab.getId());
            }
            List<LLMMessage> messages = this.view.getMessages();
            session.setMessages(LLMSessionUtil.toJson(messages));
            session.touch();
            boolean empty = messages.isEmpty();
            LLMSession snapshot = LLMSessionUtil.snapshot(session);
            long saveVersion = this.sessionSaveVersion.incrementAndGet();
            TaskManager.cancel(this.sessionSaveTask);
            this.sessionSaveTask = TaskManager.startAsync(() -> {
                synchronized (this.sessionSaveLock) {
                    if (saveVersion != this.sessionSaveVersion.get()) {
                        return;
                    }
                    if (empty && !this.sessionStore.exist(snapshot.getUid())) {
                        return;
                    }
                    if (!this.sessionStore.save(snapshot)) {
                        JulLog.error("save llm session fail: " + snapshot.getUid());
                    }
                }
            });
        });
    }

    private void syncCurrentSessionAfterHistory() {
        if (this.currentSession == null) {
            return;
        }
        LLMSession stored = this.sessionStore.selectOne(this.currentSession.getUid());
        if (stored == null) {
            TaskManager.cancel(this.sessionSaveTask);
            this.currentSession = null;
            this.createNewSession(false);
            return;
        }
        this.currentSession.setTitle(stored.getTitle());
        this.currentSession.setCreateTime(stored.getCreateTime());
        this.currentSession.setUpdateTime(stored.getUpdateTime());
        this.currentSession.setTokenUsageStats(stored.getTokenUsageStats());
        this.view.setTokenUsageStats(this.currentSession.getTokenUsageStats());
    }
}
