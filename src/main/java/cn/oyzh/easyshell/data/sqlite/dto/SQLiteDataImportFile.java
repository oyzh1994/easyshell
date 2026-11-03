package cn.oyzh.easyshell.data.sqlite.dto;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlite.table.SQLiteTable;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.fx.gui.text.field.ChooseFileTextField;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.File;
import java.util.List;

/**
 * SQLite 数据导入文件。
 *
 * @since 2026-11-02
 */
public class SQLiteDataImportFile {

    private ShellSqliteClient client;

    private ObjectProperty<File> fileProperty;

    private String targetTableName;

    public void setClient(ShellSqliteClient client) {
        this.client = client;
    }

    public ObjectProperty<File> fileProperty() {
        if (this.fileProperty == null) {
            this.fileProperty = new SimpleObjectProperty<>();
        }
        return this.fileProperty;
    }

    public File getFile() {
        return this.fileProperty == null ? null : this.fileProperty.get();
    }

    public String getFilePath() {
        File file = this.getFile();
        return file == null ? null : file.getPath();
    }

    public String getFileName() {
        File file = this.getFile();
        return file == null ? null : file.getName();
    }

    public void setFile(File file) {
        this.fileProperty().set(file);
    }

    public ChooseFileTextField getFilePathControl() {
        ChooseFileTextField textField = new ChooseFileTextField();
        textField.setText(this.getFilePath());
        textField.setOnSelectedFile(this::setFile);
        this.fileProperty().addListener((observable, oldValue, newValue) -> textField.setText(newValue.getPath()));
        TableViewUtil.selectRowOnMouseClicked(textField);
        return textField;
    }

    public FXComboBox<String> getTargetTableControl() {
        FXComboBox<String> comboBox = new FXComboBox<>();
        List<SQLiteTable> tables = this.client == null ? List.of() : this.client.selectTables();
        for (SQLiteTable table : tables) {
            comboBox.addItem(table.getName());
        }
        comboBox.selectFirstIfNull(this.getTargetTableName());
        comboBox.selectedItemChanged((observable, oldValue, newValue) -> this.setTargetTableName(newValue));
        TableViewUtil.selectRowOnMouseClicked(comboBox);
        return comboBox;
    }

    public String getTableName() {
        String fileName = this.getFileName();
        if (StringUtil.isBlank(fileName)) {
            return fileName;
        }
        int index = fileName.lastIndexOf('.');
        return index > 0 ? fileName.substring(0, index) : fileName;
    }

    public String getTargetTableName() {
        return StringUtil.isBlank(this.targetTableName) ? this.getTableName() : this.targetTableName;
    }

    public void setTargetTableName(String targetTableName) {
        this.targetTableName = targetTableName;
    }
}
