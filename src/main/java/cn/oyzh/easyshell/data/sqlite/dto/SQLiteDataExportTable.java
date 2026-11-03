package cn.oyzh.easyshell.data.sqlite.dto;

import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.fx.db.DBName;
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
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * SQLite 数据导出表。
 *
 * @since 2026-11-02
 */
public class SQLiteDataExportTable implements DBName {

    private String name;

    private List<Map<String, Object>> records;

    private List<SQLiteDataExportColumn> columns;

    private StringProperty filePathProperty;

    private BooleanProperty selectedProperty;

    private ObjectProperty<FileExtensionFilter> extensionProperty;

    public BooleanProperty selectedProperty() {
        if (this.selectedProperty == null) {
            this.selectedProperty = new SimpleBooleanProperty(false);
            this.selectedProperty.addListener((observable, oldValue, newValue) -> {
                if (Boolean.TRUE.equals(newValue) && this.getFilePath() == null) {
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
            this.setSelected(Boolean.TRUE.equals(newValue));
            ignoreChanged.set(false);
        });
        this.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (!ignoreChanged.get()) {
                checkBox.setSelected(Boolean.TRUE.equals(newValue));
            }
        });
        TableViewUtil.selectRowOnMouseClicked(checkBox);
        return checkBox;
    }

    public StringProperty filePathProperty() {
        if (this.filePathProperty == null) {
            this.filePathProperty = new SimpleStringProperty();
        }
        return this.filePathProperty;
    }

    public String getFilePath() {
        return this.filePathProperty == null ? null : this.filePathProperty.get();
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
        this.extensionProperty().addListener(observable -> {
            textField.setExtension(this.getExtension());
            textField.setInitFileName(this.fileName());
            this.updateFilePath();
        });
        TableViewUtil.rowOnCtrlS(textField);
        TableViewUtil.selectRowOnMouseClicked(textField);
        return textField;
    }

    public ObjectProperty<FileExtensionFilter> extensionProperty() {
        if (this.extensionProperty == null) {
            this.extensionProperty = new SimpleObjectProperty<>();
            this.extensionProperty.addListener((observable) -> this.updateFilePath());
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
        return this.name;
    }

    public void columns(List<? extends SQLiteColumn> columns) {
        this.columns = new ArrayList<>();
        for (SQLiteColumn column : columns) {
            SQLiteDataExportColumn exportColumn = new SQLiteDataExportColumn();
            exportColumn.setName(column.getName());
            exportColumn.setType(column.getType());
            exportColumn.setSize(column.getSize());
            exportColumn.setNotNull(column.isNotNull());
            exportColumn.setPrimaryKey(column.isPrimaryKey());
            exportColumn.setAutoIncrement(column.isAutoIncrement());
            exportColumn.setDefaultValue(column.getDefaultValue());
            exportColumn.setHidden(column.isHidden());
            exportColumn.setUnique(column.isUnique());
            exportColumn.setCollate(column.getCollate());
            exportColumn.setCheck(column.getCheck());
            exportColumn.setGeneratedExpression(column.getGeneratedExpression());
            exportColumn.setGeneratedStorage(column.getGeneratedStorage());
            this.columns.add(exportColumn);
        }
    }

    public List<SQLiteDataExportColumn> getColumns() {
        return this.columns == null ? List.of() : this.columns;
    }

    public List<SQLiteColumn> selectedColumns() {
        List<SQLiteColumn> selected = new ArrayList<>();
        for (SQLiteDataExportColumn column : this.getColumns()) {
            if (column.isSelected()) {
                selected.add(column);
            }
        }
        return selected;
    }

    public List<String> selectedColumnNames() {
        return this.selectedColumns().stream().map(SQLiteColumn::getName).toList();
    }

    public boolean hasColumns() {
        return !this.getColumns().isEmpty();
    }

    private void updateFilePath() {
        if (this.isSelected() || this.getFilePath() != null) {
            this.setFilePath(FXChooser.getDesktopDirectory() + File.separator + this.fileName());
        }
    }

    public List<Map<String, Object>> getRecords() {
        return this.records;
    }

    public void setRecords(List<Map<String, Object>> records) {
        this.records = records;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
}
