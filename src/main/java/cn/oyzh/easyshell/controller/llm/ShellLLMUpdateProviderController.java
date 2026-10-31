package cn.oyzh.easyshell.controller.llm;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.fx.llm.LLMApiTypeComboBox;
import cn.oyzh.easyshell.fx.llm.LLMProviderTemplateTextField;
import cn.oyzh.easyshell.llm.LLMApiType;
import cn.oyzh.easyshell.llm.LLMProvider;
import cn.oyzh.easyshell.llm.LLMProviderFetchUtil;
import cn.oyzh.easyshell.llm.LLMProviderTemplate;
import cn.oyzh.easyshell.llm.LLMProviderTemplateManager;
import cn.oyzh.easyshell.store.llm.LLMProviderStore;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.PasswordTextField;
import cn.oyzh.fx.gui.text.field.SelectTextFiled;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.button.FXButton;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.i18n.I18nResourceBundle;
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
    private LLMProviderTemplateTextField template;

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

    @FXML
    private FXButton fetchModelButton;

    private LLMProvider provider;

    private final LLMProviderStore providerStore = LLMProviderStore.INSTANCE;

    @FXML
    private void fetchModels() {
        if (!this.baseUrl.validate()) {
            return;
        }
        LLMApiType type = this.apiType.getSelectedType();
        if (type == null) {
            return;
        }
        String currentModel = this.model.getTextTrim();
        this.fetchModelButton.disable();
        LLMProviderFetchUtil.fetch(
                this.baseUrl.getTextTrim(),
                this.sk.getPassword(),
                type,
                models -> this.applyFetchedModels(models, currentModel),
                this::showFetchModelsFailure,
                () -> this.fetchModelButton.enable()
        );
    }

    private void applyFetchedModels(List<String> models, String currentModel) {
        if (models == null || models.isEmpty()) {
            MessageBox.warn(I18nResourceBundle.i18nStringWithDefault("shell.llm.fetchModelsEmpty", "No models found"), this.getWindow());
            return;
        }
        this.model.setItemList(models);
        if (StringUtil.isBlank(currentModel)) {
            this.model.text(models.getFirst());
        }
        FXUtil.runWait( this.model::requestFocus);
        MessageBox.okToast(I18nResourceBundle.i18nStringWithDefault("shell.llm.fetchModelsSuccess", "Model list updated"), this.getWindow());
    }

    private void showFetchModelsFailure(Exception ex) {
        String message = I18nResourceBundle.i18nStringWithDefault("shell.llm.fetchModelsFail", "Failed to fetch models");
        String detail = ex == null ? null : ex.getClass().getSimpleName();
        if (StringUtil.isNotBlank(detail)) {
            message += ": " + detail;
        }
        MessageBox.warn(message, this.getWindow());
    }

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
        LLMProviderTemplate template = LLMProviderTemplateManager.INSTANCE.findMatching(
                this.baseUrl.getTextTrim(), this.apiType.getSelectedType());
        if (template != null) {
            this.template.selectItem(template);
        }
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        this.template.selectedItemChanged(template -> {
            if (template != null) {
                this.baseUrl.setText(template.getBaseUrl());
                this.apiType.getSelectionModel().select(template.getApiType());
            }
        });
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.editProvider();
    }
}
