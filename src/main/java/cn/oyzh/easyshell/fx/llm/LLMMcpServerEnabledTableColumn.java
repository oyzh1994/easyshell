package cn.oyzh.easyshell.fx.llm;

import cn.oyzh.easyshell.llm.LLMMcpServer;
import cn.oyzh.easyshell.store.llm.LLMMcpServerStore;
import cn.oyzh.fx.plus.controls.table.FXTableCell;
import cn.oyzh.fx.plus.controls.table.FXTableColumn;
import cn.oyzh.fx.plus.controls.toggle.FXToggleSwitch;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import java.util.function.BiConsumer;

/**
 * MCP Server 启用状态列。
 *
 * @author oyzh
 * @since 2026-11-20
 */
public class LLMMcpServerEnabledTableColumn extends FXTableColumn<LLMMcpServer, LLMMcpServer> {

    private BiConsumer<LLMMcpServer, Boolean> enabledAction;

    private boolean allDisabled;

    public LLMMcpServerEnabledTableColumn() {
        this.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
        this.setCellFactory(this.cellFactory());
    }

    @Override
    protected Callback<TableColumn<LLMMcpServer, LLMMcpServer>, TableCell<LLMMcpServer, LLMMcpServer>> cellFactory() {
        return column -> new EnabledCell();
    }

    public void setEnabledAction(BiConsumer<LLMMcpServer, Boolean> enabledAction) {
        this.enabledAction = enabledAction;
    }

    public void setAllDisabled(boolean allDisabled) {
        this.allDisabled = allDisabled;
    }

    private final class EnabledCell extends FXTableCell<LLMMcpServer, LLMMcpServer> {

        private final FXToggleSwitch toggleSwitch = new FXToggleSwitch();

        private LLMMcpServer server;

        private boolean updating;

        private EnabledCell() {
            this.setAlignment(Pos.CENTER);
            this.toggleSwitch.setText(null);
            this.toggleSwitch.selectedChanged((observable, oldValue, newValue) -> {
                if (this.updating || this.server == null) {
                    return;
                }
                if (LLMMcpServerEnabledTableColumn.this.enabledAction != null) {
                    LLMMcpServerEnabledTableColumn.this.enabledAction.accept(this.server, newValue);
                    return;
                }
                if (!LLMMcpServerStore.INSTANCE.setEnabled(this.server, newValue)) {
                    this.updating = true;
                    this.toggleSwitch.setSelected(oldValue);
                    this.updating = false;
                    return;
                }
                TableView<LLMMcpServer> tableView = this.getTableView();
                if (tableView != null) {
                    tableView.refresh();
                }
            });
        }

        @Override
        protected void updateItem(LLMMcpServer item, boolean empty) {
            super.updateItem(item, empty);
            super.setText(null);
            if (empty || item == null) {
                this.server = null;
                super.setGraphic(null);
                return;
            }
            this.server = item;
            this.updating = true;
            this.toggleSwitch.setSelected(item.isEnabled() || item.isEnabling());
            this.toggleSwitch.setDisable(
                    LLMMcpServerEnabledTableColumn.this.allDisabled || item.isEnabling() || item.isChecking());
            this.updating = false;
            super.setGraphic(this.toggleSwitch);
        }
    }
}
