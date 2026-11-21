package cn.oyzh.easyshell.controller.llm;

import cn.oyzh.easyshell.fx.llm.LLMProviderTableView;
import cn.oyzh.easyshell.llm.capability.LLMModelCapabilityStore;
import cn.oyzh.easyshell.llm.provider.LLMProvider;
import cn.oyzh.easyshell.store.llm.LLMProviderStore;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.i18n.I18nResourceBundle;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * LLM provider list.
 *
 * @author oyzh
 * @since 2026-10-30
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "llm/shellLLMProviderList.fxml"
)
public class ShellLLMProviderListController extends StageController {

    @FXML
    private LLMProviderTableView providerTableView;

    @FXML
    private ClearableTextField searchField;

    private final LLMProviderStore providerStore = LLMProviderStore.INSTANCE;

    private List<LLMProvider> allItems = new ArrayList<>();

    @FXML
    private void add() {
        StageAdapter adapter = ShellViewFactory.addLLMProvider(this.getWindow());
        if (adapter == null) {
            return;
        }
        LLMProvider provider = adapter.getProp("provider");
        if (provider != null) {
            this.allItems.add(provider);
            this.applyFilter();
        }
    }

    @FXML
    private void update() {
        LLMProvider provider = this.providerTableView.getSelectedItem();
        if (provider == null) {
            return;
        }
        StageAdapter adapter = ShellViewFactory.updateLLMProvider(provider, this.getWindow());
        if (adapter == null) {
            return;
        }
        LLMProvider updated = adapter.getProp("provider");
        if (updated != null) {
            this.applyFilter();
        }
    }

    @FXML
    private void delete() {
        LLMProvider provider = this.providerTableView.getSelectedItem();
        if (provider == null) {
            return;
        }
        String message = String.format(I18nResourceBundle.i18nString("shell.llm.deleteProviderConfirm"), provider.getName());
        if (MessageBox.confirm(message, this.getWindow())) {
            if (this.providerStore.delete(provider)) {
                LLMModelCapabilityStore.INSTANCE.deleteByProvider(provider.getUid());
                this.allItems.remove(provider);
                this.applyFilter();
                MessageBox.okToast(I18nHelper.operationSuccess(), this.getWindow());
            } else {
                MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
            }
        }
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
        this.providerTableView.setAddAction(this::add);
        this.providerTableView.setEditAction(this::update);
        this.providerTableView.setDeleteAction(this::delete);
        this.allItems = new ArrayList<>(this.providerStore.selectList());
        this.applyFilter();
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    private void applyFilter() {
        this.providerTableView.setItem(LLMListSearchUtil.filter(
                this.allItems,
                this.searchField == null ? null : this.searchField.getText(),
                LLMProvider::getName,
                LLMProvider::getApiTypeName,
                LLMProvider::getBaseUrl,
                LLMProvider::getDefaultModel,
                LLMProvider::getModelsText));
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.llmProvider();
    }
}
