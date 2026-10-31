package cn.oyzh.easyshell.controller.llm;

import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.llm.LLMApiType;
import cn.oyzh.easyshell.llm.LLMModelFetcher;
import cn.oyzh.easyshell.llm.ui.LLMApiTypeComboBox;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.PasswordTextField;
import cn.oyzh.fx.gui.text.field.SelectTextFiled;
import cn.oyzh.i18n.I18nResourceBundle;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.scene.control.Button;
import javafx.stage.Window;

import java.util.List;

/**
 * Provider model-list fetch action shared by add and update dialogs.
 *
 * @author oyzh
 * @since 2026-10-31
 */
public final class LLMProviderFetchUtil {

    private LLMProviderFetchUtil() {
    }

    public static void fetch(ClearableTextField baseUrl,
                             PasswordTextField sk,
                             LLMApiTypeComboBox apiType,
                             SelectTextFiled<String> model,
                             Button fetchButton,
                             Window owner) {
        if (baseUrl == null || !baseUrl.validate()) {
            return;
        }
        LLMApiType type = apiType == null ? null : apiType.getSelectedType();
        if (type == null) {
            return;
        }
        String url = baseUrl.getTextTrim();
        String apiKey = sk == null ? null : sk.getPassword();
        String currentModel = model.getTextTrim();
        if (fetchButton != null) {
            fetchButton.setDisable(true);
        }
        TaskManager.startAsync(() -> {
            List<String> models = null;
            Exception failure = null;
            try {
                models = LLMModelFetcher.fetch(url, apiKey, type);
            } catch (Exception ex) {
                failure = ex;
            }
            List<String> fetchedModels = models;
            Exception fetchFailure = failure;
            FXUtil.runLater(() -> {
                if (fetchButton != null) {
                    fetchButton.setDisable(false);
                }
                if (fetchFailure != null) {
                    MessageBox.warn(fetchFailMessage(fetchFailure), owner);
                    return;
                }
                if (fetchedModels == null || fetchedModels.isEmpty()) {
                    MessageBox.warn(I18nResourceBundle.i18nStringWithDefault("shell.llm.fetchModelsEmpty", "No models found"), owner);
                    return;
                }
                model.setItemList(fetchedModels);
                if (!StringUtil.isNotBlank(currentModel)) {
                    model.setText(fetchedModels.getFirst());
                }
                model.requestFocus();
                MessageBox.okToast(I18nResourceBundle.i18nStringWithDefault("shell.llm.fetchModelsSuccess", "Model list updated"), owner);
            });
        });
    }

    private static String fetchFailMessage(Exception ex) {
        String message = I18nResourceBundle.i18nStringWithDefault("shell.llm.fetchModelsFail", "Failed to fetch models");
        String detail = ex == null ? null : ex.getMessage();
        if (StringUtil.isNotBlank(detail)) {
            detail = ex.getClass().getSimpleName();
        }
        return StringUtil.isNotBlank(detail) ? message + ": " + detail : message;
    }

}
