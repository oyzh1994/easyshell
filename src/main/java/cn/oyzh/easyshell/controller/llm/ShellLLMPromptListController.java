package cn.oyzh.easyshell.controller.llm;

import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.fx.llm.LLMPromptTableView;
import cn.oyzh.easyshell.llm.prompt.LLMPrompt;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.store.llm.LLMPromptStore;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.easyshell.util.llm.ShellLLMListSearchUtil;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.i18n.I18nResourceBundle;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 用户提示词列表。
 *
 * @author oyzh
 * @since 2026-11-21
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "llm/shellLLMPromptList.fxml"
)
public class ShellLLMPromptListController extends StageController {

    @FXML
    private LLMPromptTableView promptTableView;

    @FXML
    private ClearableTextField searchField;

    private final LLMPromptStore promptStore = LLMPromptStore.INSTANCE;

    private final ShellSettingStore settingStore = ShellSettingStore.INSTANCE;

    private List<LLMPrompt> allItems = new ArrayList<>();

    @FXML
    private void add() {
        StageAdapter adapter = ShellViewFactory.addLLMPrompt(this.getWindow());
        if (adapter == null) {
            return;
        }
        LLMPrompt prompt = adapter.getProp("prompt");
        if (prompt != null) {
            this.allItems.add(prompt);
            this.applyFilter();
        }
    }

    @FXML
    private void update() {
        LLMPrompt prompt = this.promptTableView.getSelectedItem();
        if (prompt == null) {
            return;
        }
        StageAdapter adapter = ShellViewFactory.updateLLMPrompt(prompt, this.getWindow());
        if (adapter != null) {
            this.applyFilter();
        }
    }

    @FXML
    private void delete() {
        LLMPrompt prompt = this.promptTableView.getSelectedItem();
        if (prompt == null) {
            return;
        }
        String message = String.format(
                I18nResourceBundle.i18nString("shell.llm.prompt.deleteConfirm"),
                prompt.getName());
        if (!MessageBox.confirm(message, this.getWindow())) {
            return;
        }
        if (!this.promptStore.delete(prompt)) {
            MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
            return;
        }
        this.allItems.remove(prompt);
        this.applyFilter();
        ShellSetting setting = ShellSettingStore.SETTING;
        if (Objects.equals(setting.getLlmPromptUid(), prompt.getUid())) {
            setting.setLlmPromptUid(null);
            this.settingStore.replace(setting);
        }
        MessageBox.okToast(I18nHelper.operationSuccess(), this.getWindow());
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        this.searchField.textProperty().addListener(
                (observable, oldValue, newValue) -> this.applyFilter());
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.promptTableView.setAddAction(this::add);
        this.promptTableView.setEditAction(this::update);
        this.promptTableView.setDeleteAction(this::delete);
        this.allItems = new ArrayList<>(this.promptStore.selectList());
        this.applyFilter();
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    private void applyFilter() {
        this.promptTableView.setItem(ShellLLMListSearchUtil.filter(
                this.allItems,
                this.searchField == null ? null : this.searchField.getText(),
                LLMPrompt::getName,
                LLMPrompt::getContent));
    }

    @Override
    public String getViewTitle() {
        return I18nResourceBundle.i18nString("shell.llm.prompts");
    }
}
