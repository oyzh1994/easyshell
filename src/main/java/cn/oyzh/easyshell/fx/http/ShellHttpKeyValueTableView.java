package cn.oyzh.easyshell.fx.http;

import cn.oyzh.easyshell.http.ShellHttpKeyValue;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.plus.controls.table.FXTableColumn;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.i18n.I18nHelper;
import javafx.collections.ListChangeListener;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;

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
     * 启用列宽度
     */
    public static final double ENABLED_WIDTH = 32;

    /**
     * 键列宽度
     */
    public static final double KEY_WIDTH = 220;

    /**
     * 值列宽度
     */
    public static final double VALUE_WIDTH = 420;

    /**
     * 描述列宽度
     */
    public static final double DESCRIPTION_WIDTH = 220;

    /**
     * 行高
     */
    public static final double ROW_HEIGHT = 28;

    /**
     * 变更回调
     */
    private Runnable changeAction;

    /**
     * 是否显示描述列
     */
    private boolean descriptionVisible;

    /**
     * 描述列
     */
    private FXTableColumn<ShellHttpKeyValue, String> descriptionColumn;

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

    /**
     * 设置是否显示描述列。
     *
     * @param descriptionVisible 是否显示
     */
    public boolean isDescriptionVisible() {
        return descriptionVisible;
    }

    public void setDescriptionVisible(boolean descriptionVisible) {
        this.descriptionVisible = descriptionVisible;
        if (this.descriptionColumn != null) {
            if (descriptionVisible && !this.getColumns().contains(this.descriptionColumn)) {
                this.getColumns().add(this.descriptionColumn);
            } else if (!descriptionVisible) {
                this.getColumns().remove(this.descriptionColumn);
            }
        }
    }

    @Override
    public void initNode() {
        super.initNode();

        // 启用列
        FXTableColumn<ShellHttpKeyValue, Boolean> enabled = new FXTableColumn<>();
        enabled.setPrefWidth(ENABLED_WIDTH);
        enabled.setMinWidth(ENABLED_WIDTH);
        enabled.setMaxWidth(ENABLED_WIDTH);
        enabled.setEditable(true);
        enabled.setSortable(false);
        enabled.setResizable(false);
        enabled.setCellValueFactory(cell -> cell.getValue().enabledProperty());
        enabled.setCellFactory(column -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();

            {
                this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                this.checkBox.setOnAction(event -> {
                    if (this.getTableRow() != null && this.getTableRow().getItem() != null) {
                        this.getTableRow().getItem().setEnabled(this.checkBox.isSelected());
                    }
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || this.getTableRow() == null || this.getTableRow().getItem() == null) {
                    this.setGraphic(null);
                    return;
                }
                this.checkBox.setSelected(this.getTableRow().getItem().isEnabled());
                this.setGraphic(this.checkBox);
            }
        });

        // 键列
        FXTableColumn<ShellHttpKeyValue, String> key = new FXTableColumn<>();
        key.setText(I18nHelper.key());
        key.setPrefWidth(KEY_WIDTH);
        key.setEditable(true);
        key.setSortable(false);
        key.setCellValueFactory(cell -> cell.getValue().keyProperty());
        key.setEditable(false);
        key.setCellFactory(column -> new ShellHttpTextInputCell(
                ShellHttpKeyValue::getKey, ShellHttpKeyValue::setKey,
                ShellI18nHelper.httpKeyPlaceholder()));

        // 值列
        FXTableColumn<ShellHttpKeyValue, String> value = new FXTableColumn<>();
        value.setText(I18nHelper.value());
        value.setPrefWidth(VALUE_WIDTH);
        value.setEditable(true);
        value.setSortable(false);
        value.setCellValueFactory(cell -> cell.getValue().valueProperty());
        value.setEditable(false);
        value.setCellFactory(column -> new ShellHttpTextInputCell(
                ShellHttpKeyValue::getValue, ShellHttpKeyValue::setValue,
                ShellI18nHelper.httpValuePlaceholder()));

        this.descriptionColumn = new FXTableColumn<>();
        this.descriptionColumn.setText(ShellI18nHelper.httpDescription());
        this.descriptionColumn.setPrefWidth(DESCRIPTION_WIDTH);
        this.descriptionColumn.setEditable(true);
        this.descriptionColumn.setSortable(false);
        this.descriptionColumn.setCellValueFactory(cell -> cell.getValue().descriptionProperty());
        this.descriptionColumn.setEditable(false);
        this.descriptionColumn.setCellFactory(column -> new ShellHttpTextInputCell(
                ShellHttpKeyValue::getDescription, ShellHttpKeyValue::setDescription,
                ShellI18nHelper.httpDescriptionPlaceholder()));

        java.util.List<FXTableColumn<ShellHttpKeyValue, ?>> columns = new java.util.ArrayList<>(
                java.util.List.of(enabled, key, value));
        if (this.descriptionVisible) {
            columns.add(this.descriptionColumn);
        }
        this.getColumns().setAll(columns);
        this.setEditable(true);
        this.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        this.setFixedCellSize(ROW_HEIGHT);

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
        // 兼容 setItems 早于 initNode 的情况
        this.getItems().forEach(this::observe);
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
            row.descriptionProperty().addListener((observable, oldValue, newValue) -> this.fireChange());
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
