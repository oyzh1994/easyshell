package cn.oyzh.easyshell.fx.llm;

import cn.oyzh.easyshell.llm.model.LLMCustomModel;
import cn.oyzh.easyshell.store.llm.LLMCustomModelStore;
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
 * 自定义模型启用状态列。
 *
 * @author oyzh
 * @since 2026-11-24
 */
public class LLMCustomModelEnabledTableColumn extends FXTableColumn<LLMCustomModel, LLMCustomModel> {

    public LLMCustomModelEnabledTableColumn() {
        this.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
        this.setCellFactory(this.cellFactory());
    }

    @Override
    protected Callback<TableColumn<LLMCustomModel, LLMCustomModel>,
            TableCell<LLMCustomModel, LLMCustomModel>> cellFactory() {
        return column -> new EnabledCell();
    }

    private static final class EnabledCell extends FXTableCell<LLMCustomModel, LLMCustomModel> {

        private final FXToggleSwitch toggleSwitch = new FXToggleSwitch();

        private LLMCustomModel model;

        private boolean updating;

        private EnabledCell() {
            this.setAlignment(Pos.CENTER);
            this.toggleSwitch.setText(null);
            this.toggleSwitch.selectedChanged((observable, oldValue, newValue) -> {
                if (this.updating || this.model == null) {
                    return;
                }
                if (!LLMCustomModelStore.INSTANCE.setEnabled(this.model, newValue)) {
                    this.updating = true;
                    this.toggleSwitch.setSelected(oldValue);
                    this.updating = false;
                    return;
                }
                TableView<LLMCustomModel> table = this.getTableView();
                if (table != null) {
                    table.refresh();
                }
            });
        }

        @Override
        protected void updateItem(LLMCustomModel item, boolean empty) {
            super.updateItem(item, empty);
            super.setText(null);
            if (empty || item == null) {
                this.model = null;
                super.setGraphic(null);
                return;
            }
            this.model = item;
            this.updating = true;
            this.toggleSwitch.setSelected(item.isEnabled());
            this.updating = false;
            super.setGraphic(this.toggleSwitch);
        }
    }
}
