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
 * 编辑 Agent Skill。
 *
 * @author oyzh
 * @since 2026-11-21
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "llm/shellLLMUpdateSkill.fxml"
)
public class ShellLLMUpdateSkillController extends StageController {

    @FXML
    private ClearableTextField name;

    @FXML
    private ClearableTextField sourcePath;

    private LLMSkill skill;

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
        ShellLLMSkillUiUtil.showSource(this.sourcePath, source, true);
    }

    @FXML
    private void update() {
        if (this.skill == null
                || !this.name.validate()
                || !ShellLLMSkillUiUtil.validateName(this.name.getTextTrim(), this.skill.getUid(),
                this.skillStore, this.getWindow())) {
            return;
        }
        if (this.source != null
                && !ShellLLMSkillUiUtil.validateSource(this.source, this.getWindow())) {
            return;
        }
        try {
            LLMSkill updated = LLMSkillManager.INSTANCE.update(
                    this.skill, this.name.getTextTrim(), this.source);
            this.setProp("skill", updated);
            MessageBox.okToast(I18nHelper.operationSuccess(), this.getWindow());
            this.closeWindow();
        } catch (Exception ex) {
            MessageBox.warn(ex.getMessage(), this.getWindow());
        }
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.skill = this.getProp("skill");
        if (this.skill != null) {
            this.name.setText(this.skill.getName());
            this.sourcePath.setText(this.skill.getFilePath());
        }
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nResourceBundle.i18nString("shell.llm.skill.edit");
    }
}
