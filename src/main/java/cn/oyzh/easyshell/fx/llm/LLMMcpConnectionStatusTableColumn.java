package cn.oyzh.easyshell.fx.llm;

import cn.oyzh.easyshell.llm.mcp.LLMMcpConnectionState;
import cn.oyzh.easyshell.llm.mcp.LLMMcpServer;
import cn.oyzh.fx.plus.controls.table.FXTableCell;
import cn.oyzh.fx.plus.controls.table.FXTableColumn;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.util.Callback;

/**
 * MCP Server 连接状态列。
 *
 * @author oyzh
 * @since 2026-11-20
 */
public class LLMMcpConnectionStatusTableColumn extends FXTableColumn<LLMMcpServer, LLMMcpServer> {

    public LLMMcpConnectionStatusTableColumn() {
        this.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
        this.setCellFactory(this.cellFactory());
    }

    @Override
    protected Callback<TableColumn<LLMMcpServer, LLMMcpServer>, TableCell<LLMMcpServer, LLMMcpServer>> cellFactory() {
        return column -> new ConnectionStatusCell();
    }

    private static final class ConnectionStatusCell extends FXTableCell<LLMMcpServer, LLMMcpServer> {

        @Override
        protected void updateItem(LLMMcpServer item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                super.setText(null);
                this.setTooltip(null);
                this.setStyle("");
                return;
            }
            super.setText(item.getConnectionStatusText());
            LLMMcpConnectionState state = item.getConnectionState();
            this.setStyle(switch (state) {
                case UNKNOWN -> "-fx-text-fill: -color-fg-muted;";
                case CHECKING -> "-fx-text-fill: -color-accent-fg;";
                case CONNECTED -> "-fx-text-fill: -color-success-fg;";
                case FAILED -> "-fx-text-fill: -color-danger-fg;";
            });
            String message = item.getConnectionMessage();
            this.setTooltip(message == null || message.isBlank() ? null : new Tooltip(message));
        }
    }
}
