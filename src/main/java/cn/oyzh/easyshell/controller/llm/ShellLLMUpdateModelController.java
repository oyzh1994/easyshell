package cn.oyzh.easyshell.controller.llm;

import cn.oyzh.easyshell.fx.llm.LLMModelContextWindowComboBox;
import cn.oyzh.easyshell.llm.model.LLMCustomModel;
import cn.oyzh.easyshell.store.llm.LLMCustomModelStore;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.i18n.I18nResourceBundle;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * 编辑自定义 LLM 模型。
 *
 * @author oyzh
 * @since 2026-11-24
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "llm/shellLLMUpdateModel.fxml"
)
public class ShellLLMUpdateModelController extends StageController {

    @FXML
    private ClearableTextField name;

    @FXML
    private ClearableTextField modelId;

    @FXML
    private LLMModelContextWindowComboBox contextWindow;

    @FXML
    private FXCheckBox tools;

    @FXML
    private FXCheckBox image;

    @FXML
    private FXCheckBox pdf;

    @FXML
    private FXCheckBox audio;

    @FXML
    private FXCheckBox video;

    @FXML
    private FXCheckBox thinking;

    private LLMCustomModel model;

    private final LLMCustomModelStore modelStore = LLMCustomModelStore.INSTANCE;

    @FXML
    private void update() {
        if (!this.name.validate() || !this.modelId.validate()) {
            return;
        }
        try {
            this.model.setName(this.name.getTextTrim());
            this.model.setModelId(this.modelId.getTextTrim());
            this.model.setContextWindowTokens(this.contextWindow.getSelectedTokens());
            this.model.setTools(this.tools.isSelected());
            this.model.setImage(this.image.isSelected());
            this.model.setPdf(this.pdf.isSelected());
            this.model.setAudio(this.audio.isSelected());
            this.model.setVideo(this.video.isSelected());
            this.model.setThinking(this.thinking.isSelected());
            if (!this.modelStore.update(this.model)) {
                MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
                return;
            }
            this.setProp("model", this.model);
            MessageBox.okToast(I18nHelper.operationSuccess(), this.getWindow());
            this.closeWindow();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, this.getWindow());
        }
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        this.model = this.getProp("model");
        this.name.setText(this.model.getName());
        this.modelId.setText(this.model.getModelId());
        this.contextWindow.setTokens(this.model.getContextWindowTokens());
        this.tools.setSelected(this.model.isTools());
        this.image.setSelected(this.model.isImage());
        this.pdf.setSelected(this.model.isPdf());
        this.audio.setSelected(this.model.isAudio());
        this.video.setSelected(this.model.isVideo());
        this.thinking.setSelected(this.model.isThinking());
        super.onWindowShown(event);
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nResourceBundle.i18nStringWithDefault("shell.llm.editModel", "Edit model");
    }
}
