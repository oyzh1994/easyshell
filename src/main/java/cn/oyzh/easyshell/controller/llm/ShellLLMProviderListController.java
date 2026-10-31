package cn.oyzh.easyshell.controller.llm;

import cn.oyzh.easyshell.fx.llm.LLMProviderTableView;
import cn.oyzh.easyshell.llm.LLMProvider;
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
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

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

    private final LLMProviderStore providerStore = LLMProviderStore.INSTANCE;

    @FXML
    private void add() {
        StageAdapter adapter = ShellViewFactory.addLLMProvider(this.getWindow());
        if (adapter == null) {
            return;
        }
        LLMProvider provider = adapter.getProp("provider");
        if (provider != null) {
            this.providerTableView.addItem(provider);
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
            this.providerTableView.refresh();
        }
    }

    @FXML
    private void delete() {
        LLMProvider provider = this.providerTableView.getSelectedItem();
        if (provider == null) {
            return;
        }
        String message = "\u5220\u9664\u5927\u6A21\u578B\u4F9B\u5E94\u5546 " + provider.getName() + " ?";
        if (MessageBox.confirm(message, this.getWindow())) {
            if (this.providerStore.delete(provider)) {
                this.providerTableView.removeItem(provider);
                MessageBox.okToast(I18nHelper.operationSuccess(), this.getWindow());
            } else {
                MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
            }
        }
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.providerTableView.setAddAction(this::add);
        this.providerTableView.setEditAction(this::update);
        this.providerTableView.setDeleteAction(this::delete);
        this.providerTableView.setItem(this.providerStore.selectList());
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nResourceBundle.i18nString("base.llm");
    }
}
