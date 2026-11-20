package cn.oyzh.easyshell.controller.llm;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.controller.ShellMainController;
import cn.oyzh.easyshell.fx.llm.LLMView;
import cn.oyzh.easyshell.internal.ShellLLMTools;
import cn.oyzh.easyshell.llm.LLMAssistant;
import cn.oyzh.easyshell.llm.LLMExecutionMode;
import cn.oyzh.easyshell.llm.LLMFileContentUtil;
import cn.oyzh.easyshell.llm.agent.LLMAgentEventType;
import cn.oyzh.easyshell.llm.mcp.LLMMcpServer;
import cn.oyzh.easyshell.llm.mcp.LLMMcpToolSession;
import cn.oyzh.easyshell.llm.LLMMessage;
import cn.oyzh.easyshell.llm.provider.LLMProvider;
import cn.oyzh.easyshell.llm.LLMResponseUtil;
import cn.oyzh.easyshell.llm.LLMSafeChatMemory;
import cn.oyzh.easyshell.llm.session.LLMSession;
import cn.oyzh.easyshell.llm.session.LLMSessionTab;
import cn.oyzh.easyshell.llm.session.LLMSessionUtil;
import cn.oyzh.easyshell.llm.LLMTabCapability;
import cn.oyzh.easyshell.llm.LLMThinkingLevel;
import cn.oyzh.easyshell.llm.agent.LLMAgentEvent;
import cn.oyzh.easyshell.llm.agent.LLMAgentListener;
import cn.oyzh.easyshell.llm.agent.LLMAgentRuntime;
import cn.oyzh.easyshell.llm.agent.LLMAgentTrace;
import cn.oyzh.easyshell.llm.agent.LLMAgentTraceFormatter;
import cn.oyzh.easyshell.llm.tool.LLMToolAudit;
import cn.oyzh.easyshell.llm.tool.LLMToolNameStrategy;
import cn.oyzh.easyshell.llm.tool.LLMTools;
import cn.oyzh.easyshell.llm.LLMUtil;
import cn.oyzh.easyshell.store.llm.LLMSessionStore;
import cn.oyzh.easyshell.store.llm.LLMMcpServerStore;
import cn.oyzh.easyshell.tabs.ShellTabPane;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.fx.plus.controller.SubStageController;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.i18n.I18nResourceBundle;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.memory.ChatMemoryService;
import dev.langchain4j.service.tool.ToolArgumentsErrorHandler;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * 聊天业务
 *
 * @author oyzh
 * @since 2025-03-03
 */
public class ChatController extends SubStageController {

    @FXML
    private FXVBox root;

    @FXML
    private LLMView view;

    private ShellTabPane tabPane;

    private LLMSession currentSession;

    private final LLMSessionStore sessionStore = LLMSessionStore.INSTANCE;

    private Future<?> sessionSaveTask;

    private boolean syncingSessionState;

    @FXML
    private void llm() {
        ShellViewFactory.llmProviders(this.getWindow());
        this.view.refreshProviders();
    }

    @FXML
    private void mcp() {
        ShellViewFactory.llmMcpServers(this.getWindow());
    }

    /**
     * 新建会话
     */
    @FXML
    private void newSession() {
        if (this.view.isMessageEmpty() || MessageBox.confirm(LLMSessionUtil.newSessionConfirm(), this.getWindow())) {
            this.createNewSession();
        }
    }

    /**
     * 会话历史
     */
    @FXML
    private void history() {
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
        this.view.setSendValidator(this::validateSessionForSend);
        this.view.getModelComboBox().selectedItemChanged((observable, oldValue, newValue) -> this.initLLM());
        this.view.getSessionTabComboBox().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
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

        private volatile Thread thread;

        private volatile LLMMessage placeholder;

        private volatile List<LLMTools> tools;

        private volatile LLMMcpToolSession mcpSession;

        private LLMAgentRuntime agentRuntime;

        private List<LLMMcpServer> mcpServers;

        private LLMProvider provider;

        private LLMThinkingLevel thinkingLevel;

        private LLMSessionTab sessionTab;

        private LLMTools sessionTools;

        private LLMExecutionMode executionMode;

        private String model;

        private boolean streaming;

        private ChatMemory chatMemory;

        private String sessionId;

        private String sessionTabId;
    }

    private LLMAssistant initAssistant(LLMRequest request) {
        LLMProvider provider = request.provider;
        // 服务
        AiServices<LLMAssistant> services = AiServices.builder(LLMAssistant.class);
        List<LLMTools> tools = new ArrayList<>();
        request.tools = tools;
        // 内置工具
        ShellLLMTools internalTools = new ShellLLMTools(this.tabPane);
        tools.add(internalTools);
        if (request.sessionTools != null) {
            tools.add(request.sessionTools);
        }
        // MCP 工具，单个 Server 失败不会影响其他工具
        request.mcpSession = LLMMcpToolSession.connect(
                request.mcpServers, progress -> this.updateMcpProgress(request, progress));
        if (request.mcpSession.hasClients()) {
            services.toolProvider(request.mcpSession.getToolProvider());
        }
        if (!request.mcpSession.getErrors().isEmpty()) {
            String error = String.format(I18nResourceBundle.i18nString("shell.llm.mcp.testFail"),
                    String.join("; ", request.mcpSession.getErrors()));
            this.view.addErrorMessage(error, request.sessionTabId);
        }
        LLMExecutionMode executionMode = request.executionMode;
        // 工具相关配置
        services.tools(tools.toArray());
        services.maxToolCallingRoundTrips(100);
        services.toolArgumentsErrorHandler(ToolArgumentsErrorHandler.sendExceptionMessageToLlm());
        services.hallucinatedToolNameStrategy(LLMToolNameStrategy.create(tools, request.mcpSession.getToolNames()));
        // 工具调用审计
        LLMToolAudit audit = new LLMToolAudit(tools,
                request.sessionId, request.sessionTabId,
                provider.getName(), request.model, executionMode);
        audit.registerToolSources(request.mcpSession.getToolSources());
        services.afterToolExecution(audit::record);
        String systemMessage = request.sessionTools == null
                ? internalTools.systemMessage(executionMode)
                : internalTools.toolSystemMessage() + "\n\n"
                + request.sessionTools.systemMessage(executionMode);
        String localSystemMessage = systemMessage;
        if (request.mcpSession.hasClients()) {
            systemMessage += "\n\n" + request.mcpSession.getSystemMessage();
        }
        services.systemMessage(systemMessage);
        // 使用当前会话历史初始化内存，当前消息由大模型服务负责追加
        services.chatMemory(request.chatMemory);
        if (request.streaming) {
            // 创建模型
            StreamingChatModel model = LLMUtil.crateStreamingChatModel(provider, request.thinkingLevel);
            ChatModel agentModel = LLMUtil.createChatModel(provider, request.thinkingLevel);
            services.streamingChatModel(model);
            // 创建工具
            this.assistant = services.build();
            request.agentRuntime = this.createAgentRuntime(agentModel, model, tools,
                    request.mcpSession, localSystemMessage,
                    internalTools.systemMessage(executionMode), executionMode, audit);
        } else {
            // 创建模型
            ChatModel model = LLMUtil.createChatModel(provider, request.thinkingLevel);
            services.chatModel(model);
            // 创建工具
            this.assistant = services.build();
            request.agentRuntime = this.createAgentRuntime(model, null, tools,
                    request.mcpSession, localSystemMessage,
                    internalTools.systemMessage(executionMode), executionMode, audit);
        }
        return this.assistant;
    }

    private LLMAgentRuntime createAgentRuntime(ChatModel chatModel,
                                               StreamingChatModel streamingChatModel,
                                               List<LLMTools> tools,
                                               LLMMcpToolSession mcpSession,
                                               String localSystemMessage,
                                               String internalSystemMessage,
                                               LLMExecutionMode executionMode,
                                               LLMToolAudit audit) {
        String mcpSystemMessage = mcpSession == null ? "" : mcpSession.getSystemMessage();
        return new LLMAgentRuntime(chatModel, streamingChatModel, tools, mcpSession,
                localSystemMessage, internalSystemMessage, mcpSystemMessage,
                LLMUtil.executionSystemMessage(executionMode),
                LLMUtil.languageSystemMessage(), audit);
    }

    private ChatMemory buildChatMemory(LLMMessage currentMessage) {
        String memoryId = this.currentSession == null ? ChatMemoryService.DEFAULT : this.currentSession.getUid();
        MessageWindowChatMemory memory = MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(this.view.getLlmMaxMessages())
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
            if (!this.validateSessionForSend()) {
                return;
            }
            LLMProvider provider = this.validateProviderForSend();
            if (provider == null) {
                return;
            }
            LLMRequest request = new LLMRequest();
            request.provider = provider;
            request.model = this.view.getSelectedModel();
            request.streaming = this.view.isStreamingEnabled();
            request.chatMemory = this.buildChatMemory(message);
            request.sessionTab = this.view.getSelectedSession();
            request.thinkingLevel = this.view.getSelectedThinkingLevel();
            request.executionMode = this.view.getSelectedExecutionMode();
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
                this.clearPlaceholder(request);
                this.view.addErrorMessage(ex.getMessage(), message.getSessionTabId());
                this.finishRequest(request, true);
            } else {
                this.closeRequestResources(request);
            }
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
                AtomicBoolean firstResponse = new AtomicBoolean(true);
                AtomicBoolean firstThinking = new AtomicBoolean(true);
                assistant.chatStream(contents)
                        .onPartialThinking(token -> {
                            if (!this.isRequestActive(request) || StringUtil.isBlank(token.text())) {
                                return;
                            }
                            if (firstThinking.getAndSet(false)
                                    && Objects.equals(targetPlaceholder.getMessage(), ShellI18nHelper.thinkingPlaceholder())) {
                                this.view.replaceMessage(targetPlaceholder, "");
                            }
                            this.view.appendThinking(targetPlaceholder, token.text());
                        })
                        .onPartialResponse(token -> {
                            if (!this.isRequestActive(request)) {
                                return;
                            }
                            if (firstResponse.getAndSet(false)) {
                                this.view.replaceMessage(targetPlaceholder, token);
                            } else {
                                this.view.appendMessage(targetPlaceholder, token);
                            }
                        })
                        .onError(ex -> {
                            if (!this.isRequestActive(request)) {
                                return;
                            }
                            JulLog.error("llm error", ex);
                            this.view.addErrorMessage(ex.getMessage(), message.getSessionTabId());
                            this.finishRequest(request, true);
                        })
                        .onCompleteResponse(response -> this.completeStreamingRequest(request, targetPlaceholder, response))
                        .start();
            } else {
                try {
                    ChatResponse response = assistant.chatResponse(contents);
                    if (!this.isRequestActive(request)) {
                        return;
                    }
                    String thinking = LLMResponseUtil.finalThinking(response);
                    String finalText = LLMResponseUtil.finalText(response);
                    this.view.replaceThinking(placeholder, thinking == null ? "" : thinking);
                    this.view.replaceMessage(placeholder, finalText == null ? "" : finalText);
                    if (finalText == null) {
                        this.view.addErrorMessage(ShellI18nHelper.emptyResponse(), message.getSessionTabId());
                    }
                    if (LLMResponseUtil.outputTruncated(response)) {
                        this.view.addErrorMessage(ShellI18nHelper.outputTruncated(), message.getSessionTabId());
                    }
                    this.finishRequest(request, true);
                } catch (Exception ex) {
                    if (!this.isRequestActive(request)) {
                        return;
                    }
                    JulLog.error("llm error", ex);
                    this.view.addErrorMessage(ex.getMessage(), message.getSessionTabId());
                    this.finishRequest(request, true);
                }
            }
        } catch (Exception ex) {
            if (!this.isRequestActive(request)) {
                return;
            }
            JulLog.error("llm request error", ex);
            this.view.addErrorMessage(ex.getMessage(), message.getSessionTabId());
            this.finishRequest(request, true);
        }
    }

    private void executeAgentRequest(LLMRequest request,
                                     LLMMessage message,
                                     LLMMessage placeholder) {
        AtomicBoolean firstResponse = new AtomicBoolean(true);
        AtomicBoolean firstThinking = new AtomicBoolean(true);
        LinkedHashMap<String, LLMAgentTrace> traces = new LinkedHashMap<>();
        try {
            this.view.replaceAgentTrace(placeholder, "");
            ChatResponse response = request.agentRuntime.execute(
                    message.getMessage(),
                    request.chatMemory.messages(),
                    new LLMAgentListener() {
                        @Override
                        public void onEvent(LLMAgentEvent event) {
                            if (!isRequestActive(request) || event == null
                                    || event.type() == LLMAgentEventType.COMPLETED) {
                                return;
                            }
                            view.replaceMessage(placeholder, agentEventText(event));
                        }

                        @Override
                        public void onPartialThinking(String text) {
                            if (!isRequestActive(request) || StringUtil.isBlank(text)) {
                                return;
                            }
                            if (firstThinking.getAndSet(false) && firstResponse.get()) {
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
                            traces.put(trace.id(), trace);
                            view.replaceAgentTrace(placeholder, LLMAgentTraceFormatter.format(traces.values()));
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
            this.view.addErrorMessage(ex.getMessage(), message.getSessionTabId());
            this.finishRequest(request, true);
        }
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
            this.view.setBusy(false);
            if (save) {
                this.saveCurrentSession();
            }
        }
        this.closeRequestResources(request);
    }

    private synchronized void closeRequestResources(LLMRequest request) {
        List<LLMTools> requestTools = request.tools;
        request.tools = null;
        if (requestTools != null) {
            for (LLMTools tools : requestTools) {
                try {
                    tools.close();
                } catch (Exception ex) {
                    JulLog.warn("close llm tools failed", ex);
                }
            }
        }
        LLMMcpToolSession mcpSession = request.mcpSession;
        request.mcpSession = null;
        if (mcpSession != null) {
            mcpSession.close();
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
        this.view.getSessionTabComboBox().addItem(sessionTab);
    }

    private void selectCurrentMainTab() {
        if (this.tabPane == null) {
            return;
        }
        Tab selected = this.tabPane.getSelectionModel().getSelectedItem();
        if (selected instanceof LLMTabCapability capability) {
            this.view.getSessionTabComboBox().selectTab(capability.getTabId());
        } else if (this.view.getSessionTabComboBox().getSelectedTab() == null
                && !this.view.getSessionTabComboBox().getItems().isEmpty()) {
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

    private void saveCurrentSession() {
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
        TaskManager.cancel(this.sessionSaveTask);
        this.sessionSaveTask = TaskManager.startAsync(() -> {
            if (empty && !this.sessionStore.exist(session.getUid())) {
                return;
            }
            if (!this.sessionStore.save(session)) {
                JulLog.error("save llm session fail: " + session.getUid());
            }
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
    }
}
