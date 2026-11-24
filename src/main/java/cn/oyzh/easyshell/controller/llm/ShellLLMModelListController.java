package cn.oyzh.easyshell.controller.llm;

import cn.oyzh.easyshell.fx.llm.LLMCustomModelTableView;
import cn.oyzh.easyshell.llm.model.LLMCustomModel;
import cn.oyzh.easyshell.store.llm.LLMCustomModelStore;
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

/**
 * 自定义 LLM 模型列表。
 *
 * @author oyzh
 * @since 2026-11-24
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "llm/shellLLMModelList.fxml"
)
public class ShellLLMModelListController extends StageController {

    @FXML
    private LLMCustomModelTableView modelTableView;

    @FXML
    private ClearableTextField searchField;

    private final LLMCustomModelStore modelStore = LLMCustomModelStore.INSTANCE;

    private List<LLMCustomModel> allItems = new ArrayList<>();

    @FXML
    private void add() {
        StageAdapter adapter = ShellViewFactory.addLLMModel(this.getWindow());
        if (adapter != null && adapter.getProp("model") instanceof LLMCustomModel model) {
            this.allItems.add(model);
            this.applyFilter();
        }
    }

    @FXML
    private void update() {
        LLMCustomModel model = this.modelTableView.getSelectedItem();
        if (model == null) {
            return;
        }
        StageAdapter adapter = ShellViewFactory.updateLLMModel(model, this.getWindow());
        if (adapter != null) {
            this.applyFilter();
        }
    }

    @FXML
    private void delete() {
        LLMCustomModel model = this.modelTableView.getSelectedItem();
        if (model == null) {
            return;
        }
        String message = String.format(
                I18nResourceBundle.i18nStringWithDefault(
                        "shell.llm.model.deleteConfirm", "Delete model %s ?"),
                model.getDisplayName());
        if (!MessageBox.confirm(message, this.getWindow())) {
            return;
        }
        if (!this.modelStore.delete(model)) {
            MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
            return;
        }
        this.allItems.remove(model);
        this.applyFilter();
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
        this.modelTableView.setAddAction(this::add);
        this.modelTableView.setEditAction(this::update);
        this.modelTableView.setDeleteAction(this::delete);
        this.allItems = new ArrayList<>(this.modelStore.selectList());
        this.applyFilter();
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    private void applyFilter() {
        this.modelTableView.setItem(ShellLLMListSearchUtil.filter(
                this.allItems,
                this.searchField == null ? null : this.searchField.getText(),
                LLMCustomModel::getDisplayName,
                LLMCustomModel::getModelId,
                LLMCustomModel::getCapabilitiesText));
    }

    @Override
    public String getViewTitle() {
        return I18nResourceBundle.i18nStringWithDefault("shell.llm.models", "Models");
    }
}
