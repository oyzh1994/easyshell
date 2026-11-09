package cn.oyzh.easyshell.fx.http;

import cn.oyzh.easyshell.http.ShellHttpKeyValue;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.text.field.ChooseFileTextField;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.controls.table.FXTableColumn;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;

import java.util.Collections;
import java.util.Objects;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * HTTP form-data 表格，支持文本和文件字段。
 *
 * @author oyzh
 * @since 2026-11-08
 */
public class ShellHttpFormDataTableView extends FXTableView<ShellHttpKeyValue> {

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
        enabled.setPrefWidth(ShellHttpKeyValueTableView.ENABLED_WIDTH);
        enabled.setMinWidth(ShellHttpKeyValueTableView.ENABLED_WIDTH);
        enabled.setMaxWidth(ShellHttpKeyValueTableView.ENABLED_WIDTH);
        enabled.setEditable(true);
        enabled.setSortable(false);
        enabled.setResizable(false);
        enabled.setCellValueFactory(cell -> cell.getValue().enabledProperty());
        enabled.setCellFactory(CheckBoxTableCell.forTableColumn(enabled));

        // 键列
        FXTableColumn<ShellHttpKeyValue, String> key = new FXTableColumn<>();
        key.setText(I18nHelper.key());
        key.setPrefWidth(ShellHttpKeyValueTableView.KEY_WIDTH);
        key.setEditable(true);
        key.setSortable(false);
        key.setCellValueFactory(cell -> cell.getValue().keyProperty());
        key.setEditable(false);
        key.setCellFactory(column -> new ShellHttpTextInputCell(
                ShellHttpKeyValue::getKey, ShellHttpKeyValue::setKey,
                ShellI18nHelper.httpKeyPlaceholder()));

        // 值/文件列
        FXTableColumn<ShellHttpKeyValue, String> value = new FXTableColumn<>();
        value.setText(I18nHelper.value());
        value.setPrefWidth(ShellHttpKeyValueTableView.VALUE_WIDTH);
        value.setEditable(false);
        value.setSortable(false);
        value.setCellValueFactory(cell -> cell.getValue().valueProperty());
        value.setCellFactory(column -> new ValueCell());

        // 描述列，仅用于说明，不参与请求
        FXTableColumn<ShellHttpKeyValue, String> description = new FXTableColumn<>();
        description.setText(ShellI18nHelper.httpDescription());
        description.setPrefWidth(ShellHttpKeyValueTableView.DESCRIPTION_WIDTH);
        description.setEditable(false);
        description.setSortable(false);
        description.setCellValueFactory(cell -> cell.getValue().descriptionProperty());
        description.setCellFactory(column -> new ShellHttpTextInputCell(
                ShellHttpKeyValue::getDescription, ShellHttpKeyValue::setDescription,
                ShellI18nHelper.httpDescriptionPlaceholder()));

        this.getColumns().setAll(java.util.List.of(enabled, key, value, description));
        this.setEditable(true);
        this.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        this.setFixedCellSize(ShellHttpKeyValueTableView.ROW_HEIGHT);

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
            row.fileProperty().addListener((observable, oldValue, newValue) -> this.fireChange());
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

    /**
     * 值/文件单元格
     */
    private class ValueCell extends TableCell<ShellHttpKeyValue, String> {

        /**
         * 文本输入
         */
        private final ClearableTextField textField = new ClearableTextField();

        /**
         * 文件选择
         */
        private final ChooseFileTextField fileField = new ChooseFileTextField();

        /**
         * 值/文件容器
         */
        private final StackPane editorBox = new StackPane(this.textField, this.fileField);

        /**
         * 文件标识
         */
        private final CheckBox fileBox = new CheckBox(I18nHelper.file());

        /**
         * 内容容器
         */
        private final HBox box = new HBox(this.editorBox, this.fileBox);

        /**
         * 当前行
         */
        private ShellHttpKeyValue row;

        /**
         * 文件状态监听
         */
        private final ChangeListener<Boolean> fileListener = (observable, oldValue, newValue) -> this.render();

        /**
         * 是否正在更新
         */
        private boolean updating;

        {
            this.box.setPrefHeight(28);
            this.box.setSpacing(6);
            this.box.setAlignment(Pos.CENTER_LEFT);
            this.textField.setRealHeight(26);
            this.textField.setPromptText(ShellI18nHelper.httpValuePlaceholder());
            this.box.prefWidthProperty().bind(this.widthProperty().subtract(6));
            TableViewUtil.selectRowOnMouseClicked(this.textField);
            TableViewUtil.selectRowOnMouseClicked(this.fileField);
            TableViewUtil.selectRowOnMouseClicked(this.fileBox);
            HBox.setHgrow(this.editorBox, Priority.ALWAYS);
            this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            this.textField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!this.updating && this.row != null) {
                    this.row.setValue(newValue);
                }
            });
            this.fileField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!this.updating && this.row != null) {
                    this.row.setValue(newValue);
                }
            });
            this.fileBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (!this.updating && this.row != null) {
                    this.row.setFile(newValue);
                }
            });
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            ShellHttpKeyValue newRow = empty || this.getTableRow() == null
                    ? null : this.getTableRow().getItem();
            if (this.row != newRow) {
                if (this.row != null) {
                    this.row.fileProperty().removeListener(this.fileListener);
                }
                this.row = newRow;
                if (this.row != null) {
                    this.row.fileProperty().addListener(this.fileListener);
                }
            }
            if (this.row == null) {
                this.setGraphic(null);
                return;
            }
            this.updating = true;
            if (!this.textField.isFocused() && !Objects.equals(this.textField.getText(), this.row.getValue())) {
                this.textField.setText(this.row.getValue());
            }
            if (!Objects.equals(this.fileField.getText(), this.row.isFile() ? this.row.getValue() : "")) {
                this.fileField.setText(this.row.isFile() ? this.row.getValue() : "");
            }
            this.fileBox.setSelected(this.row.isFile());
            this.updating = false;
            this.render();
        }

        /**
         * 根据文件状态切换编辑器。
         */
        private void render() {
            if (this.row == null) {
                return;
            }
            boolean file = this.row.isFile();
            this.updating = true;
            if (!this.textField.isFocused() && !Objects.equals(this.textField.getText(), this.row.getValue())) {
                this.textField.setText(this.row.getValue());
            }
            String fileValue = file ? this.row.getValue() : "";
            if (!Objects.equals(this.fileField.getText(), fileValue)) {
                this.fileField.setText(fileValue);
            }
            this.fileBox.setSelected(file);
            this.updating = false;
            this.textField.setVisible(!file);
            this.textField.setManaged(!file);
            this.fileField.setVisible(file);
            this.fileField.setManaged(file);
            if (this.getGraphic() != this.box) {
                this.setGraphic(this.box);
            }
        }
    }
}
