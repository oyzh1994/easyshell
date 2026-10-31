package cn.oyzh.easyshell.controller.llm;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.controller.MainController;
import cn.oyzh.easyshell.controller.ShellMainController;
import cn.oyzh.easyshell.llm.LLMAssistant;
import cn.oyzh.easyshell.llm.LLMMessage;
import cn.oyzh.easyshell.llm.LLMProvider;
import cn.oyzh.easyshell.llm.LLMSessionTab;
import cn.oyzh.easyshell.llm.LLMTabCapability;
import cn.oyzh.easyshell.llm.LLMThinkingLevel;
import cn.oyzh.easyshell.llm.LLMTools;
import cn.oyzh.easyshell.llm.LLMUtil;
import cn.oyzh.easyshell.llm.ui.LLMView;
import cn.oyzh.easyshell.tabs.ShellTabPane;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controller.SubStageController;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;

import java.util.List;
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

    @FXML
    private void llm() {
        ShellViewFactory.llmProviders(this.getWindow());
        this.view.refreshProviders();
    }

    @Override
    protected void bindListeners() {
        this.initLLM();
        this.view.getModelComboBox().selectedItemChanged((observable, oldValue, newValue) -> {
            this.initLLM();
        });
        this.view.getSessionTabComboBox().selectedItemChanged((observable, oldValue, newValue) -> {
            this.initLLM();
        });
        this.view.getThinkingLevelComboBox().selectedItemChanged((observable, oldValue, newValue) -> {
            this.initLLM();
        });
        this.root.visibleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && this.view.isMessageEmpty() && this.tabPane.getSelectedItem() instanceof LLMTabCapability capability) {
                this.view.getSessionTabComboBox().selectTab(capability.getTabId());
            }
        });
        super.bindListeners();
    }

    private LLMAssistant assistant;

    private LLMAssistant initAssistant() {
        // 提供商
        LLMProvider provider = this.view.getSelectedProvider();
        if (provider == null) {
            MessageBox.warn(I18nHelper.pleaseSelectProvider(), this.getWindow());
            return null;
        }
        // 会话
        LLMSessionTab sessionTab = this.view.getSelectedSession();
        LLMTools tools = null;
        if (sessionTab != null && sessionTab.getToolsFunc() != null) {
            tools = sessionTab.getToolsFunc().get();
        }
        // 思考等级
        LLMThinkingLevel thinkingLevel = this.view.getSelectedThinkingLevel();
        if (this.view.isStreamingEnabled()) {
            // 创建模型
            StreamingChatModel model = LLMUtil.crateStreamingChatModel(provider, thinkingLevel);
            // 创建工具
            this.assistant = AiServices.builder(LLMAssistant.class)
                    .streamingChatModel(model)
                    .tools(tools)
                    .build();
        } else {
            // 创建模型
            ChatModel model = LLMUtil.createChatModel(provider, thinkingLevel);
            // 创建工具
            this.assistant = AiServices.builder(LLMAssistant.class)
                    .chatModel(model)
                    .tools(tools)
                    .build();
        }
        return this.assistant;
    }

    /**
     * 初始化大模型
     */
    private void initLLM() {
        this.assistant = null;
        this.view.setSendHandler(message -> {
            LLMAssistant assistant = this.initAssistant();
            if (assistant == null) {
                this.view.addErrorMessage(I18nHelper.initLLMFail());
                return;
            }
            boolean streaming = this.view.isStreamingEnabled();
            ThreadUtil.startVirtual(() -> {
                LLMMessage message1 = this.view.addAssistantMessage(I18nHelper.thinking());
                if (streaming) {
                    AtomicBoolean first = new AtomicBoolean(true);
                    assistant.chatStream(message.getMessage())
                            .onPartialResponse(token -> {
                                if (first.get()) {
                                    first.set(false);
                                    this.view.replaceMessage(message1, token);
                                } else {
                                    this.view.appendMessage(message1, token);
                                }
                            })
                            .onError(ex -> {
                                JulLog.error("llm error", ex);
                                this.view.addErrorMessage(ex.getMessage());
                            })
                            .start();
                } else {
                    try {
                        String response = assistant.chat(message.getMessage());
                        this.view.replaceMessage(message1, response);
                    } catch (Exception ex) {
                        JulLog.error("llm error", ex);
                        this.view.addErrorMessage(ex.getMessage());
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
            if (c.next()) {
                if (c.wasAdded() || c.wasReplaced()) {
                    List<? extends Tab> tabs = c.getAddedSubList();
                    for (Tab tab : tabs) {
                        if (tab instanceof LLMTabCapability capability) {
                            LLMSessionTab tab1 = new LLMSessionTab();
                            tab1.setId(capability.getTabId());
                            tab1.setName(capability.getTabName());
                            tab1.setToolsFunc(capability.getToolsFunc());
                            this.view.getSessionTabComboBox().addItem(tab1);
                        }
                    }
                }
                if (c.wasRemoved() || c.wasReplaced()) {
                    List<? extends Tab> tabs = c.getRemoved();
                    for (Tab tab : tabs) {
                        if (tab instanceof LLMTabCapability capability) {
                            this.view.getSessionTabComboBox().removeTab(capability.getTabId());
                        }
                    }
                }
            }
        });
    }
}
