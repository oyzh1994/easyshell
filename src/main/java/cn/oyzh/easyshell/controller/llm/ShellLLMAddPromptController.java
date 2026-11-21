package cn.oyzh.easyshell.controller.llm;

import cn.oyzh.easyshell.llm.prompt.LLMPrompt;
import cn.oyzh.easyshell.llm.prompt.LLMPromptUtil;
import cn.oyzh.easyshell.store.llm.LLMPromptStore;
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
 * 添加用户提示词。
 *
 * @author oyzh
 * @since 2026-11-21
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "llm/shellLLMAddPrompt.fxml"
)
public class ShellLLMAddPromptController extends StageController {

    @FXML
    private ClearableTextField name;

    @FXML
    private MarkdownEditor content;

    private final LLMPromptStore promptStore = LLMPromptStore.INSTANCE;

    @FXML
    private void add() {
        if (!LLMPromptControllerUtil.validate(this.name, this.content,
                this.promptStore, null, this.getWindow())) {
            return;
        }
        try {
            LLMPrompt prompt = new LLMPrompt();
            prompt.setName(this.name.getTextTrim());
            prompt.setContent(this.content.getTextTrim());
            if (this.promptStore.insert(prompt)) {
                this.setProp("prompt", prompt);
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
        LLMPromptControllerUtil.prepareEditor(this.content);
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nResourceBundle.i18nString("shell.llm.prompt.add");
    }
}
