package cn.oyzh.easyshell.fx.llm;

import cn.oyzh.easyshell.llm.skill.LLMSkill;
import cn.oyzh.easyshell.llm.skill.LLMSkillValidationState;
import cn.oyzh.fx.plus.controls.table.FXTableCell;
import cn.oyzh.fx.plus.controls.table.FXTableColumn;
import cn.oyzh.i18n.I18nResourceBundle;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
 * Skill ZIP 校验状态列。
 *
 * @author oyzh
 * @since 2026-11-21
 */
public class LLMSkillValidationStateTableColumn extends FXTableColumn<LLMSkill, LLMSkill> {

    public LLMSkillValidationStateTableColumn() {
        this.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
        this.setCellFactory(this.cellFactory());
    }

    @Override
    protected Callback<TableColumn<LLMSkill, LLMSkill>, TableCell<LLMSkill, LLMSkill>> cellFactory() {
        return column -> new ValidationCell();
    }

    private static final class ValidationCell extends FXTableCell<LLMSkill, LLMSkill> {

        @Override
        protected void updateItem(LLMSkill item, boolean empty) {
            super.updateItem(item, empty);
            this.getStyleClass().removeAll(
                    "llm-skill-valid", "llm-skill-invalid", "llm-skill-checking");
            if (empty || item == null) {
                super.setText(null);
                super.setTooltip(null);
                return;
            }
            LLMSkillValidationState state = item.getValidationState();
            super.setText(displayText(state));
            super.setTooltip(item.getValidationMessage() == null
                    ? null
                    : new javafx.scene.control.Tooltip(item.getValidationMessage()));
            if (state == LLMSkillValidationState.NORMAL) {
                this.getStyleClass().add("llm-skill-valid");
            } else if (state == LLMSkillValidationState.CHECKING) {
                this.getStyleClass().add("llm-skill-checking");
            } else if (state != LLMSkillValidationState.UNKNOWN) {
                this.getStyleClass().add("llm-skill-invalid");
            }
        }

        private static String displayText(LLMSkillValidationState state) {
            return switch (state) {
                case UNKNOWN -> I18nResourceBundle.i18nString(
                        "shell.llm.skill.validation.unknown");
                case CHECKING -> I18nResourceBundle.i18nString(
                        "shell.llm.skill.validation.checking");
                case NORMAL -> I18nResourceBundle.i18nString(
                        "shell.llm.skill.validation.normal");
                case FILE_MISSING -> I18nResourceBundle.i18nString(
                        "shell.llm.skill.validation.fileMissing");
                case HASH_INVALID -> I18nResourceBundle.i18nString(
                        "shell.llm.skill.validation.hashInvalid");
                case INVALID -> I18nResourceBundle.i18nString(
                        "shell.llm.skill.validation.invalid");
            };
        }
    }
}
