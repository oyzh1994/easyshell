package cn.oyzh.easyshell.controller.llm;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.fx.llm.LLMSkillTableView;
import cn.oyzh.easyshell.llm.skill.LLMSkill;
import cn.oyzh.easyshell.llm.skill.LLMSkillManager;
import cn.oyzh.easyshell.llm.skill.LLMSkillValidationState;
import cn.oyzh.easyshell.store.llm.LLMSkillStore;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.easyshell.util.llm.ShellLLMListSearchUtil;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.gui.svg.glyph.RefreshSVGGlyph;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.chooser.FileChooserHelper;
import cn.oyzh.fx.plus.chooser.FileExtensionFilter;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.i18n.I18nResourceBundle;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Agent Skill 列表。
 *
 * @author oyzh
 * @since 2026-11-21
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "llm/shellLLMSkillList.fxml"
)
public class ShellLLMSkillListController extends StageController {

    @FXML
    private LLMSkillTableView skillTableView;

    @FXML
    private RefreshSVGGlyph refreshGlyph;

    @FXML
    private ClearableTextField searchField;

    private final LLMSkillStore skillStore = LLMSkillStore.INSTANCE;

    private List<LLMSkill> allItems = new ArrayList<>();

    private boolean checking;

    @FXML
    private void add() {
        StageAdapter adapter = ShellViewFactory.addLLMSkill(this.getWindow());
        if (adapter != null && adapter.getProp("skill") instanceof LLMSkill skill) {
            this.allItems.add(skill);
            this.applyFilter();
        }
    }

    @FXML
    private void update() {
        LLMSkill skill = this.skillTableView.getSelectedItem();
        if (skill == null) {
            return;
        }
        StageAdapter adapter = ShellViewFactory.updateLLMSkill(skill, this.getWindow());
        if (adapter != null) {
            this.applyFilter();
        }
    }

    @FXML
    private void delete() {
        LLMSkill skill = this.skillTableView.getSelectedItem();
        if (skill == null) {
            return;
        }
        String message = String.format(
                I18nResourceBundle.i18nString("shell.llm.skill.deleteConfirm"), skill.getName());
        if (!MessageBox.confirm(message, this.getWindow())) {
            return;
        }
        if (!this.skillStore.delete(skill)) {
            MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
            return;
        }
        LLMSkillManager.INSTANCE.deleteFiles(skill.getUid());
        this.allItems.remove(skill);
        this.applyFilter();
        MessageBox.okToast(I18nHelper.operationSuccess(), this.getWindow());
    }

    @FXML
    private void export() {
        LLMSkill skill = this.skillTableView.getSelectedItem();
        if (skill == null) {
            return;
        }
        File target = FileChooserHelper.save(
                I18nResourceBundle.i18nString("shell.llm.skill.export"),
                skill.getName() + ".zip",
                new FileExtensionFilter("ZIP", "*.zip"),
                this.getWindow());
        if (target == null) {
            return;
        }
        try {
            LLMSkillManager.INSTANCE.export(skill, target.toPath());
            MessageBox.okToast(I18nResourceBundle.i18nString("shell.llm.skill.exportSuccess"),
                    this.getWindow());
        } catch (Exception ex) {
            MessageBox.warn(StringUtil.isBlank(ex.getMessage())
                    ? I18nResourceBundle.i18nString("shell.llm.skill.exportFailed")
                    : ex.getMessage(), this.getWindow());
        }
    }

    @FXML
    private void refresh() {
        this.checkAll();
    }

    private void checkAll() {
        if (this.checking) {
            return;
        }
        List<LLMSkill> skills = this.allItems;
        if (skills == null || skills.isEmpty()) {
            return;
        }
        this.checking = true;
        this.refreshGlyph.setDisable(true);
        this.refreshGlyph.startWaiting();
        for (LLMSkill skill : skills) {
            skill.setValidationState(LLMSkillValidationState.CHECKING, null);
        }
        this.skillTableView.refresh();
        ThreadUtil.startVirtual(() -> {
            try {
                for (LLMSkill skill : skills) {
                    LLMSkillValidationState state = LLMSkillManager.INSTANCE.check(skill);
                    FXUtil.runLater(() -> {
                        skill.setValidationState(state, null);
                        this.skillTableView.refresh();
                    });
                }
            } finally {
                FXUtil.runLater(() -> {
                    this.checking = false;
                    this.refreshGlyph.stopWaiting();
                    this.refreshGlyph.setDisable(false);
                });
            }
        });
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        this.searchField.textProperty().addListener(
                (observable, oldValue, newValue) -> this.applyFilter());
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.skillTableView.setAddAction(this::add);
        this.skillTableView.setEditAction(this::update);
        this.skillTableView.setDeleteAction(this::delete);
        this.skillTableView.setExportAction(this::export);
        this.allItems = new ArrayList<>(this.skillStore.selectList());
        this.applyFilter();
        this.checkAll();
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    private void applyFilter() {
        this.skillTableView.setItem(ShellLLMListSearchUtil.filter(
                this.allItems,
                this.searchField == null ? null : this.searchField.getText(),
                LLMSkill::getName,
                LLMSkill::getFilePath,
                LLMSkill::getHash));
    }

    @Override
    public String getViewTitle() {
        return I18nResourceBundle.i18nString("shell.llm.skills");
    }
}
