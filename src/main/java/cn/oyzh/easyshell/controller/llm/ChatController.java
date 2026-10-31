package cn.oyzh.easyshell.controller.llm;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.controller.MainController;
import cn.oyzh.easyshell.controller.ShellMainController;
import cn.oyzh.easyshell.fx.llm.LLMView;
import cn.oyzh.easyshell.llm.LLMAssistant;
import cn.oyzh.easyshell.llm.LLMExecutionMode;
import cn.oyzh.easyshell.llm.LLMFileContentUtil;
import cn.oyzh.easyshell.llm.LLMMessage;
import cn.oyzh.easyshell.llm.LLMProvider;
import cn.oyzh.easyshell.llm.LLMSession;
import cn.oyzh.easyshell.llm.LLMSessionTab;
import cn.oyzh.easyshell.llm.LLMSessionUtil;
import cn.oyzh.easyshell.llm.LLMTabCapability;
import cn.oyzh.easyshell.llm.LLMThinkingLevel;
import cn.oyzh.easyshell.llm.LLMTools;
import cn.oyzh.easyshell.llm.LLMUtil;
import cn.oyzh.easyshell.store.llm.LLMSessionStore;
import cn.oyzh.easyshell.tabs.ShellTabPane;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controller.SubStageController;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.memory.ChatMemoryService;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;

import java.util.ArrayList;
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

    private LLMAssistant initAssistant(LLMMessage currentMessage) {
        // 提供商
        LLMProvider provider = this.view.getSelectedProvider();
        if (provider == null) {
            MessageBox.warn(I18nHelper.pleaseSelectProvider(), this.getWindow());
            return null;
        }
        // 思考等级
        LLMThinkingLevel thinkingLevel = this.view.getSelectedThinkingLevel();
        // 服务
        AiServices<LLMAssistant> services = AiServices.builder(LLMAssistant.class);
        // 会话
        LLMSessionTab sessionTab = this.view.getSelectedSession();
        LLMTools tools = null;
        if (sessionTab != null && sessionTab.getToolsFunc() != null) {
            tools = sessionTab.getToolsFunc().get();
        }
        // 执行模式
        LLMExecutionMode executionMode = this.view.getSelectedExecutionMode();
        // 工具
        if (tools != null) {
            services.tools(tools);
            services.systemMessage(tools.systemMessage(executionMode));
        }
        // 使用当前会话历史初始化内存，当前消息由大模型服务负责追加
        services.chatMemory(this.buildChatMemory(currentMessage));
        if (this.view.isStreamingEnabled()) {
            // 创建模型
            StreamingChatModel model = LLMUtil.crateStreamingChatModel(provider, thinkingLevel);
            services.streamingChatModel(model);
            // 创建工具
            this.assistant = services.build();
        } else {
            // 创建模型
            ChatModel model = LLMUtil.createChatModel(provider, thinkingLevel);
            services.chatModel(model);
            // 创建工具
            this.assistant = services.build();
        }
        return this.assistant;
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
        return memory;
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
            LLMAssistant assistant = this.initAssistant(message);
            if (assistant == null) {
                this.view.addErrorMessage(I18nHelper.initLLMFail());
                return;
            }
            boolean streaming = this.view.isStreamingEnabled();
            // 异步执行
            ThreadUtil.startVirtual(() -> {
                List<Content> contents;
                try {
                    contents = LLMFileContentUtil.toUserContents(message.getMessage(), message.getFile());
                } catch (Exception ex) {
                    JulLog.error("llm file content error", ex);
                    this.view.addErrorMessage(ex.getMessage());
                    return;
                }
                if (contents.isEmpty()) {
                    this.view.addErrorMessage(I18nHelper.invalidFile());
                    return;
                }
                LLMMessage placeholder = this.view.addAssistantMessage(I18nHelper.thinking() + "...");
                if (streaming) {
                    AtomicBoolean first = new AtomicBoolean(true);
                    assistant.chatStream(contents)
                            .onPartialResponse(token -> {
                                if (first.get()) {
                                    first.set(false);
                                    this.view.replaceMessage(placeholder, token);
                                } else {
                                    this.view.appendMessage(placeholder, token);
                                }
                            })
                            .onError(ex -> {
                                JulLog.error("llm error", ex);
                                this.view.addErrorMessage(ex.getMessage());
                                this.saveCurrentSession();
                            })
                            .onCompleteResponse(response -> this.saveCurrentSession())
                            .start();
                } else {
                    try {
                        String response = assistant.chat(contents);
                        this.view.replaceMessage(placeholder, response);
                        this.saveCurrentSession();
                    } catch (Exception ex) {
                        JulLog.error("llm error", ex);
                        this.view.addErrorMessage(ex.getMessage());
                        this.saveCurrentSession();
                    }
                }
            });
        });
    }

    @Override
    public void onStageInitialize(StageAdapter stage) {
        super.onStageInitialize(stage);
        StageManager.getPrimaryStage().onShownProperty().addListener((observable, oldValue, newValue) -> {
            StageAdapter adapter = StageManager.getAdapter(StageManager.getPrimaryStage());
            MainController controller = (MainController) adapter.controller();
            for (StageController subController : controller.getSubControllers()) {
                if (subController instanceof ShellMainController mainController) {
                    this.tabPane = mainController.getTabPane();
                }
            }
            this.init();
        });
    }

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
                                this.view.getSessionTabComboBox().getSelectionModel().clearSelection();
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
        String tabId = this.currentSession.getSessionTabId();
        if (tabId == null || this.tabPane == null) {
            return true;
        }
        for (Tab tab : this.tabPane.getTabs()) {
            if (tab instanceof LLMTabCapability capability && Objects.equals(tabId, capability.getTabId())) {
                return true;
            }
        }
        this.view.addErrorMessage(LLMSessionUtil.sessionExpired());
        return false;
    }

    private void saveCurrentSession() {
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
