package cn.oyzh.easyshell.fx.llm;

import cn.oyzh.easyshell.llm.skill.LLMSkill;
import cn.oyzh.easyshell.llm.skill.LLMSkillScriptMode;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.store.llm.LLMSkillStore;
import cn.oyzh.fx.plus.controls.table.FXTableCell;
import cn.oyzh.fx.plus.controls.table.FXTableColumn;
import cn.oyzh.fx.plus.controls.toggle.FXToggleSwitch;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.i18n.I18nResourceBundle;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

/**
 * Skill 脚本启用状态列。
 *
 * @author oyzh
 * @since 2026-11-21
 */
public class LLMSkillScriptEnabledTableColumn extends FXTableColumn<LLMSkill, LLMSkill> {

    public LLMSkillScriptEnabledTableColumn() {
        this.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
        this.setCellFactory(this.cellFactory());
    }

    @Override
    protected Callback<TableColumn<LLMSkill, LLMSkill>, TableCell<LLMSkill, LLMSkill>> cellFactory() {
        return column -> new ScriptEnabledCell();
    }

    private static final class ScriptEnabledCell extends FXTableCell<LLMSkill, LLMSkill> {

        private final FXToggleSwitch toggleSwitch = new FXToggleSwitch();

        private LLMSkill skill;

        private boolean updating;

        private ScriptEnabledCell() {
            this.setAlignment(Pos.CENTER);
            this.toggleSwitch.setText(null);
            this.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
                if (isScriptModeDisabled()) {
                    MessageBox.warn(disabledTip(), FXUtil.getWindow(this));
                    event.consume();
                }
            });
            this.toggleSwitch.selectedChanged((observable, oldValue, newValue) -> {
                if (this.updating || this.skill == null) {
                    return;
                }
                if (!LLMSkillStore.INSTANCE.setScriptEnabled(this.skill, newValue)) {
                    this.updating = true;
                    this.toggleSwitch.setSelected(oldValue);
                    this.updating = false;
                }
            });
        }

        @Override
        protected void updateItem(LLMSkill item, boolean empty) {
            super.updateItem(item, empty);
            super.setText(null);
            if (empty || item == null) {
                this.skill = null;
                super.setGraphic(null);
                return;
            }
            this.skill = item;
            boolean modeDisabled = isScriptModeDisabled();
            this.updating = true;
            this.toggleSwitch.setSelected(item.isScriptEnabled());
            this.toggleSwitch.setDisable(modeDisabled);
            this.updating = false;
            this.setTooltip(modeDisabled ? new Tooltip(disabledTip()) : null);
            super.setGraphic(this.toggleSwitch);
        }
    }

    private static boolean isScriptModeDisabled() {
        return LLMSkillScriptMode.fromOrdinal(
                ShellSettingStore.SETTING.getLlmSkillScriptMode())
                == LLMSkillScriptMode.NONE;
    }

    private static String disabledTip() {
        return I18nResourceBundle.i18nString("shell.llm.skill.scriptMode.noneTip");
    }
}
