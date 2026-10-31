package cn.oyzh.easyshell.controller.llm;

import cn.oyzh.easyshell.llm.LLMApiType;
import cn.oyzh.easyshell.llm.LLMProvider;
import cn.oyzh.easyshell.llm.ui.LLMApiTypeComboBox;
import cn.oyzh.easyshell.store.llm.LLMProviderStore;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.PasswordTextField;
import cn.oyzh.fx.gui.text.field.SelectTextFiled;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

import java.util.List;

/**
 * Add LLM provider.
 *
 * @author oyzh
 * @since 2026-10-30
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "llm/shellLLMAddProvider.fxml"
)
public class ShellLLMAddProviderController extends StageController {

    @FXML
    private ClearableTextField name;

    @FXML
    private ClearableTextField baseUrl;

    @FXML
    private PasswordTextField sk;

    @FXML
    private LLMApiTypeComboBox apiType;

    @FXML
    private SelectTextFiled<String> model;

    private final LLMProviderStore providerStore = LLMProviderStore.INSTANCE;

    @FXML
    private void add() {
        if (!this.name.validate() || !this.baseUrl.validate() || !this.apiType.validate() || !this.model.validate()) {
            return;
        }
        LLMApiType type = this.apiType.getSelectedType();
        if (type == null) {
            return;
        }
        if (type != LLMApiType.OLLAMA && !this.sk.validate()) {
            return;
        }
        try {
            String name = this.name.getTextTrim();
            String baseUrl = this.baseUrl.getTextTrim();
            String sk = this.sk.getPassword();
            List<String> models = LLMProvider.parseModels(this.model.getTextTrim());
            LLMProvider provider = new LLMProvider();
            provider.setName(name);
            provider.setBaseUrl(baseUrl);
            provider.setSk(sk);
            provider.setApiType(type.ordinal());
            provider.setModels(models);
            if (!models.isEmpty()) {
                provider.setDefaultModel(models.getFirst());
            }
            if (this.providerStore.insert(provider)) {
                this.setProp("provider", provider);
                MessageBox.okToast(I18nHelper.operationSuccess(), this.getWindow());
                this.closeWindow();
            } else {
                MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, this.getWindow());
        }
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.addProvider();
    }
}
