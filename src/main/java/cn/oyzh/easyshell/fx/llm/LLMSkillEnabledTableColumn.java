package cn.oyzh.easyshell.fx.llm;

import cn.oyzh.easyshell.llm.skill.LLMSkill;
import cn.oyzh.easyshell.store.llm.LLMSkillStore;
import cn.oyzh.fx.plus.controls.table.FXTableCell;
import cn.oyzh.fx.plus.controls.table.FXTableColumn;
import cn.oyzh.fx.plus.controls.toggle.FXToggleSwitch;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

/**
 * Skill 启用状态列。
 *
 * @author oyzh
 * @since 2026-11-21
 */
public class LLMSkillEnabledTableColumn extends FXTableColumn<LLMSkill, LLMSkill> {

    public LLMSkillEnabledTableColumn() {
        this.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
        this.setCellFactory(this.cellFactory());
    }

    @Override
    protected Callback<TableColumn<LLMSkill, LLMSkill>, TableCell<LLMSkill, LLMSkill>> cellFactory() {
        return column -> new EnabledCell();
    }

    private static final class EnabledCell extends FXTableCell<LLMSkill, LLMSkill> {

        private final FXToggleSwitch toggleSwitch = new FXToggleSwitch();

        private LLMSkill skill;

        private boolean updating;

        private EnabledCell() {
            this.setAlignment(Pos.CENTER);
            this.toggleSwitch.setText(null);
            this.toggleSwitch.selectedChanged((observable, oldValue, newValue) -> {
                if (this.updating || this.skill == null) {
                    return;
                }
                if (!LLMSkillStore.INSTANCE.setEnabled(this.skill, newValue)) {
                    this.updating = true;
                    this.toggleSwitch.setSelected(oldValue);
                    this.updating = false;
                    return;
                }
                TableView<LLMSkill> table = this.getTableView();
                if (table != null) {
                    table.refresh();
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
            this.updating = true;
            this.toggleSwitch.setSelected(item.isEnabled());
            this.updating = false;
            super.setGraphic(this.toggleSwitch);
        }
    }
}
