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
 * 新增自定义 LLM 模型。
 *
 * @author oyzh
 * @since 2026-11-24
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "llm/shellLLMAddModel.fxml"
)
public class ShellLLMAddModelController extends StageController {

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

    private final LLMCustomModelStore modelStore = LLMCustomModelStore.INSTANCE;

    @FXML
    private void add() {
        if (!this.name.validate() || !this.modelId.validate()) {
            return;
        }
        try {
            LLMCustomModel model = new LLMCustomModel();
            model.setName(this.name.getTextTrim());
            model.setModelId(this.modelId.getTextTrim());
            model.setContextWindowTokens(this.contextWindow.getSelectedTokens());
            model.setTools(this.tools.isSelected());
            model.setImage(this.image.isSelected());
            model.setPdf(this.pdf.isSelected());
            model.setAudio(this.audio.isSelected());
            model.setVideo(this.video.isSelected());
            model.setThinking(this.thinking.isSelected());
            if (!this.modelStore.insert(model)) {
                MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
                return;
            }
            this.setProp("model", model);
            MessageBox.okToast(I18nHelper.operationSuccess(), this.getWindow());
            this.closeWindow();
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
        return I18nResourceBundle.i18nStringWithDefault("shell.llm.addModel", "Add model");
    }
}
