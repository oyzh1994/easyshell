package cn.oyzh.easyshell.fx.http;

import cn.oyzh.easyshell.http.ShellHttpKeyValue;
import cn.oyzh.fx.plus.controls.table.FXTableColumn;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.i18n.I18nHelper;
import javafx.collections.ListChangeListener;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * HTTP 键值表格，用于请求参数、请求头、表单和环境变量。
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellHttpKeyValueTableView extends FXTableView<ShellHttpKeyValue> {

    /**
     * 变更回调
     */
    private Runnable changeAction;

    /**
     * 已监听的键值行
     */
    private final Set<ShellHttpKeyValue> observedRows =
            Collections.newSetFromMap(new IdentityHashMap<>());

    /**
     * 设置变更回调
     *
     * @param changeAction 变更回调
     */
    public void setChangeAction(Runnable changeAction) {
        this.changeAction = changeAction;
    }

    @Override
    public void initNode() {
        super.initNode();

        // 启用列
        FXTableColumn<ShellHttpKeyValue, Boolean> enabled = new FXTableColumn<>();
        enabled.setText(I18nHelper.enable());
        enabled.setPrefWidth(52);
        enabled.setEditable(true);
        enabled.setSortable(false);
        enabled.setCellValueFactory(cell -> cell.getValue().enabledProperty());
        enabled.setCellFactory(CheckBoxTableCell.forTableColumn(enabled));

        // 键列
        FXTableColumn<ShellHttpKeyValue, String> key = new FXTableColumn<>();
        key.setText(I18nHelper.key());
        key.setPrefWidth(220);
        key.setEditable(true);
        key.setSortable(false);
        key.setCellValueFactory(cell -> cell.getValue().keyProperty());
        key.setCellFactory(TextFieldTableCell.forTableColumn());
        key.setOnEditCommit(event -> {
            if (event.getRowValue() != null) {
                event.getRowValue().setKey(event.getNewValue());
            }
        });

        // 值列
        FXTableColumn<ShellHttpKeyValue, String> value = new FXTableColumn<>();
        value.setText(I18nHelper.value());
        value.setPrefWidth(420);
        value.setEditable(true);
        value.setSortable(false);
        value.setCellValueFactory(cell -> cell.getValue().valueProperty());
        value.setCellFactory(TextFieldTableCell.forTableColumn());
        value.setOnEditCommit(event -> {
            if (event.getRowValue() != null) {
                event.getRowValue().setValue(event.getNewValue());
            }
        });

        this.getColumns().setAll(java.util.List.of(enabled, key, value));
        this.setEditable(true);
        this.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        this.setFixedCellSize(28);

        this.getItems().addListener((ListChangeListener<ShellHttpKeyValue>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    change.getAddedSubList().forEach(this::observe);
                }
                if (change.wasRemoved()) {
                    change.getRemoved().forEach(this.observedRows::remove);
                }
            }
            this.fireChange();
        });
    }

    /**
     * 监听键值行
     *
     * @param row 键值行
     */
    private void observe(ShellHttpKeyValue row) {
        if (row != null && this.observedRows.add(row)) {
            row.enabledProperty().addListener((observable, oldValue, newValue) -> this.fireChange());
            row.keyProperty().addListener((observable, oldValue, newValue) -> this.fireChange());
            row.valueProperty().addListener((observable, oldValue, newValue) -> this.fireChange());
        }
    }

    /**
     * 触发变更回调
     */
    private void fireChange() {
        if (this.changeAction != null) {
            this.changeAction.run();
        }
    }

    /**
     * 新增空行。
     */
    public void addRow() {
        this.getItems().add(new ShellHttpKeyValue());
        this.getSelectionModel().selectLast();
        this.scrollTo(this.getItems().size() - 1);
    }

    /**
     * 删除选中行。
     */
    public void deleteSelectedRow() {
        int index = this.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            this.getItems().remove(index);
            if (!this.getItems().isEmpty()) {
                this.getSelectionModel().select(Math.min(index, this.getItems().size() - 1));
            }
        }
    }
}
