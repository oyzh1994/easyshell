package cn.oyzh.easyshell.controller.llm;

import cn.oyzh.easyshell.llm.prompt.LLMPrompt;
import cn.oyzh.easyshell.store.llm.LLMPromptStore;
import cn.oyzh.easyshell.util.llm.ShellLLMPromptControllerUtil;
import cn.oyzh.fx.editor.incubator.control.MarkdownEditor;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.FXConst;
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
 * 编辑用户提示词。
 *
 * @author oyzh
 * @since 2026-11-21
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "llm/shellLLMUpdatePrompt.fxml"
)
public class ShellLLMUpdatePromptController extends StageController {

    @FXML
    private ClearableTextField name;

    @FXML
    private MarkdownEditor content;

    private LLMPrompt prompt;

    private final LLMPromptStore promptStore = LLMPromptStore.INSTANCE;

    @FXML
    private void update() {
        if (this.prompt == null || !ShellLLMPromptControllerUtil.validate(this.name, this.content,
                this.promptStore, this.prompt.getUid(), this.getWindow())) {
            return;
        }
        try {
            this.prompt.setName(this.name.getTextTrim());
            this.prompt.setContent(this.content.getTextTrim());
            if (this.promptStore.update(this.prompt)) {
                this.setProp("prompt", this.prompt);
                MessageBox.okToast(I18nHelper.operationSuccess(), this.getWindow());
                this.closeWindow();
            } else {
                MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
            }
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.prompt = this.getProp("prompt");
        ShellLLMPromptControllerUtil.prepareEditor(this.content);
        if (this.prompt != null) {
            this.name.setText(this.prompt.getName());
            this.content.text(this.prompt.getContent());
        }
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nResourceBundle.i18nString("shell.llm.prompt.edit");
    }
}
