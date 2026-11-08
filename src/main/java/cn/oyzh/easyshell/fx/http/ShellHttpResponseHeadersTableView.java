package cn.oyzh.easyshell.fx.http;

import cn.oyzh.easyshell.http.ShellHttpKeyValue;
import cn.oyzh.fx.plus.controls.table.FXTableColumn;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;

/**
 * HTTP 响应头表格。
 *
 * @author oyzh
 * @since 2026-11-08
 */
public class ShellHttpResponseHeadersTableView extends FXTableView<ShellHttpKeyValue> {

    @Override
    public void initNode() {
        super.initNode();

        FXTableColumn<ShellHttpKeyValue, String> name = new FXTableColumn<>();
        name.setText(I18nHelper.key());
        name.setPrefWidth(240);
        name.setSortable(false);
        name.setCellValueFactory(cell -> cell.getValue().keyProperty());
        name.setCellFactory(TextFieldTableCell.forTableColumn());

        FXTableColumn<ShellHttpKeyValue, String> value = new FXTableColumn<>();
        value.setText(I18nHelper.value());
        value.setPrefWidth(600);
        value.setSortable(false);
        value.setCellValueFactory(cell -> cell.getValue().valueProperty());
        value.setCellFactory(TextFieldTableCell.forTableColumn());

        this.getColumns().setAll(java.util.List.of(name, value));
        this.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        this.setFixedCellSize(28);
        this.setEditable(false);
    }
}
