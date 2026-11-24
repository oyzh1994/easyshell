package cn.oyzh.easyshell.controller.llm;

import cn.oyzh.easyshell.llm.skill.LLMSkill;
import cn.oyzh.easyshell.llm.skill.LLMSkillManager;
import cn.oyzh.easyshell.store.llm.LLMSkillStore;
import cn.oyzh.easyshell.util.llm.ShellLLMSkillUiUtil;
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

import java.io.File;

/**
 * 添加 Agent Skill。
 *
 * @author oyzh
 * @since 2026-11-21
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "llm/shellLLMAddSkill.fxml"
)
public class ShellLLMAddSkillController extends StageController {

    @FXML
    private ClearableTextField name;

    @FXML
    private ClearableTextField sourcePath;

    private File source;

    private final LLMSkillStore skillStore = LLMSkillStore.INSTANCE;

    @FXML
    private void chooseFile() {
        this.setSource(ShellLLMSkillUiUtil.chooseFile(this.getWindow()));
    }

    @FXML
    private void chooseFolder() {
        this.setSource(ShellLLMSkillUiUtil.chooseFolder(this.getWindow()));
    }

    private void setSource(File source) {
        this.source = source;
        ShellLLMSkillUiUtil.showSource(this.sourcePath, source, false);
        if (source != null && this.name.getTextTrim().isEmpty()) {
            this.name.setText(ShellLLMSkillUiUtil.suggestedName(source));
        }
    }

    @FXML
    private void add() {
        if (!this.name.validate()
                || !ShellLLMSkillUiUtil.validateName(this.name.getTextTrim(), null,
                this.skillStore, this.getWindow())
                || !ShellLLMSkillUiUtil.validateSource(this.source, this.getWindow())) {
            return;
        }
        try {
            LLMSkill skill = LLMSkillManager.INSTANCE.create(
                    this.name.getTextTrim(), this.source);
            this.setProp("skill", skill);
            MessageBox.okToast(I18nHelper.operationSuccess(), this.getWindow());
            this.closeWindow();
        } catch (Exception ex) {
            MessageBox.warn(ex.getMessage(), this.getWindow());
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
        return I18nResourceBundle.i18nString("shell.llm.skill.add");
    }
}
