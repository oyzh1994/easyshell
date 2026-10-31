package cn.oyzh.easyshell.controller.llm;

import cn.oyzh.easyshell.controller.MainController;
import cn.oyzh.easyshell.controller.ShellMainController;
import cn.oyzh.easyshell.llm.LLMSessionTab;
import cn.oyzh.easyshell.llm.LLMTabCapability;
import cn.oyzh.easyshell.llm.ui.LLMView;
import cn.oyzh.easyshell.tabs.ShellTabPane;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controller.SubStageController;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;

import java.util.List;


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

    @Override
    protected void bindListeners() {
        super.bindListeners();
        this.view.getSessionTabComboBox().selectedItemChanged((observable, oldValue, newValue) -> {


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

    //private void update() {
    //    List<LLMSessionTab> list = new ArrayList<>();
    //    List<Tab> tabs = this.tabPane.getTabs();
    //    for (Tab tab : tabs) {
    //        if (tab instanceof LLMTabCapability capability) {
    //            LLMSessionTab tab1 = new LLMSessionTab();
    //            tab1.setId(capability.getTabId());
    //            tab1.setName(capability.getTabName());
    //            list.add(tab1);
    //        }
    //    }
    //    this.view.getSessionTabComboBox().setTabs(list);
    //}

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
