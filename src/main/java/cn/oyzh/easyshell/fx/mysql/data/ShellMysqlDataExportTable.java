package cn.oyzh.easyshell.fx.mysql.data;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.record.MysqlRecord;
import cn.oyzh.fx.gui.text.field.SaveFileTextField;
import cn.oyzh.fx.plus.chooser.FXChooser;
import cn.oyzh.fx.plus.chooser.FileExtensionFilter;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author oyzh
 * @since 2024/08/27
 */
public class ShellMysqlDataExportTable {

    /**
     * 表名称
     */
    private String name;

    /**
     * 记录列表，查询导出用
     */
    private List<MysqlRecord> records;

    /**
     * 字段列表
     */
    private List<ShellMysqlDataExportColumn> columns;

    /**
     * 文件路径属性
     */
    private StringProperty filePathProperty;

    /**
     * 是否选中属性
     */
    private BooleanProperty selectedProperty;

    /**
     * 扩展后缀属性
     */
    private ObjectProperty<FileExtensionFilter> extensionProperty;

    public BooleanProperty selectedProperty() {
        if (this.selectedProperty == null) {
            this.selectedProperty = new SimpleBooleanProperty(false);
            this.selectedProperty.addListener((observable, oldValue, newValue) -> {
                if (newValue && this.getFilePath() == null) {
                    this.updateFilePath();
                }
            });
        }
        return this.selectedProperty;
    }

    public boolean isSelected() {
        return this.selectedProperty != null && this.selectedProperty.get();
    }

    public void setSelected(boolean selected) {
        this.selectedProperty().set(selected);
    }

    public FXCheckBox getSelectedControl() {
        FXCheckBox checkBox = new FXCheckBox();
        checkBox.setSelected(this.isSelected());
        AtomicBoolean ignoreChanged = new AtomicBoolean(false);
        checkBox.selectedChanged((observable, oldValue, newValue) -> {
            ignoreChanged.set(true);
            this.setSelected(newValue);
            ignoreChanged.set(false);
        });
        this.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (!ignoreChanged.get()) {
                checkBox.setSelected(newValue);
            }
        });
        TableViewUtil.selectRowOnMouseClicked(checkBox);
        return checkBox;
    }

    public StringProperty filePathProperty() {
        if (filePathProperty == null) {
            this.filePathProperty = new SimpleStringProperty();
        }
        return this.filePathProperty;
    }

    public String getFilePath() {
        return filePathProperty == null ? null : filePathProperty.get();
    }

    public void setFilePath(String filePath) {
        this.filePathProperty().set(filePath);
    }

    public SaveFileTextField getFilePathControl() {
        SaveFileTextField textField = new SaveFileTextField();
        textField.setText(this.getFilePath());
        textField.setExtension(this.getExtension());
        textField.setInitFileName(this.fileName());
        textField.setOnSelectedFile(file -> {
            textField.setText(file.getPath());
            textField.setInitFileName(file.getName());
            this.setFilePath(file.getPath());
        });
        this.filePathProperty().addListener((observable, oldValue, newValue) -> textField.setText(newValue));
        this.extensionProperty().addListener((observable, oldValue, newValue) -> {
            textField.setExtension(newValue);
            textField.setInitFileName(this.fileName());
        });
        TableViewUtil.rowOnCtrlS(textField);
        TableViewUtil.selectRowOnMouseClicked(textField);
        return textField;
    }

    public ObjectProperty<FileExtensionFilter> extensionProperty() {
        if (this.extensionProperty == null) {
            this.extensionProperty = new SimpleObjectProperty<>();
            this.extensionProperty.addListener((observable, oldValue, newValue) -> this.updateFilePath());
        }
        return this.extensionProperty;
    }

    public FileExtensionFilter getExtension() {
        return this.extensionProperty == null ? null : this.extensionProperty.get();
    }

    public void setExtension(FileExtensionFilter extension) {
        this.extensionProperty().set(extension);
    }

    private String fileName() {
        if (this.getExtension() != null) {
            return this.name + this.getExtension().getExtension().substring(1);
        }
        return "";
    }

    public void columns(List<? extends MysqlColumn> columns) {
        this.columns = new ArrayList<>();
        for (MysqlColumn column : columns) {
            ShellMysqlDataExportColumn exportColumn = new ShellMysqlDataExportColumn();
            exportColumn.copy(column);
            this.columns.add(exportColumn);
        }
    }

    public List<MysqlColumn> columns() {
        return new ArrayList<>(this.columns);
    }

    public List<MysqlColumn> selectedColumns() {
        List<MysqlColumn> selectedColumns = new ArrayList<>();
        for (ShellMysqlDataExportColumn column : this.columns) {
            if (column.isSelected()) {
                selectedColumns.add(column);
            }
        }
        return selectedColumns;
    }

    public List<String> selectedColumnNames() {
        List<String> selectedColumns = new ArrayList<>();
        for (MysqlColumn column : this.selectedColumns()) {
            selectedColumns.add(column.getName());
        }
        return selectedColumns;
    }

    public boolean hasColumns() {
        return CollectionUtil.isNotEmpty(this.columns);
    }

    private void updateFilePath() {
        if (this.isSelected() || this.getFilePath() != null) {
            this.setFilePath(FXChooser.getDesktopDirectory() + File.separator + this.fileName());
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ShellMysqlDataExportColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<ShellMysqlDataExportColumn> columns) {
        this.columns = columns;
    }

    public List<MysqlRecord> getRecords() {
        return records;
    }

    public void setRecords(List<MysqlRecord> records) {
        this.records = records;
    }
}
