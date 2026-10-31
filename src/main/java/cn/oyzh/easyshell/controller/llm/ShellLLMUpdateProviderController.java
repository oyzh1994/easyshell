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
 * Update LLM provider.
 *
 * @author oyzh
 * @since 2026-10-30
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "llm/shellLLMUpdateProvider.fxml"
)
public class ShellLLMUpdateProviderController extends StageController {

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

    private LLMProvider provider;

    private final LLMProviderStore providerStore = LLMProviderStore.INSTANCE;

    @FXML
    private void update() {
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
            this.provider.setName(name);
            this.provider.setBaseUrl(baseUrl);
            this.provider.setSk(sk);
            this.provider.setApiType(type.ordinal());
            this.provider.setModels(models);
            if (!models.isEmpty()) {
                this.provider.setDefaultModel(models.getFirst());
            } else {
                this.provider.setDefaultModel(null);
            }
            if (this.providerStore.update(this.provider)) {
                this.setProp("provider", this.provider);
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
        this.provider = this.getProp("provider");
        this.name.setText(this.provider.getName());
        this.baseUrl.setText(this.provider.getBaseUrl());
        this.sk.setText(this.provider.getSk());
        this.apiType.setApiType(this.provider.getApiType());
        this.model.setText(this.provider.getModelsText());
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.editProvider();
    }
}
