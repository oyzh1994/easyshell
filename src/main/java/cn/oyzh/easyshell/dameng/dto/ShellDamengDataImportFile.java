package cn.oyzh.easyshell.dameng.dto;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.fx.dameng.table.DamengTableComboBox;
import cn.oyzh.fx.gui.text.field.ChooseFileTextField;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.fx.plus.window.StageManager;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.File;

/**
 * @author oyzh
 * @since 2024/08/30
 */
public class ShellDamengDataImportFile {

    private String schema;

    public void setSchema(String schema) {
        this.schema = schema;
    }

    private ShellDamengClient dbClient;

    public void setDbClient(ShellDamengClient dbClient) {
        this.dbClient = dbClient;
    }

    /**
     * 文件路径属性
     */
    private ObjectProperty<File> fileProperty;

    /**
     * 目标表名称
     */
    private String targetTableName;

    public ObjectProperty<File> fileProperty() {
        if (fileProperty == null) {
            this.fileProperty = new SimpleObjectProperty<>();
        }
        return this.fileProperty;
    }

    public File getFile() {
        return fileProperty == null ? null : fileProperty.get();
    }

    public String getFilePath() {
        File file = getFile();
        return file == null ? null : file.getPath();
    }

    public String getFileName() {
        File file = getFile();
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

    public DamengTableComboBox getTargetTableControl() {
        DamengTableComboBox comboBox = new DamengTableComboBox();
        //String dbName = CacheHelper.get("mysql:dbName");
        //ShellDamengClient dbClient = CacheHelper.get("mysql:dbClient");
        StageManager.showMask(() -> comboBox.init(this.schema, this.getTableName(), this.dbClient));
        comboBox.selectedItemChanged((observable, oldValue, newValue) -> {
            this.setTargetTableName(newValue);
        });
        TableViewUtil.selectRowOnMouseClicked(comboBox);
        return comboBox;
    }

    public String getTableName() {
        String fileName = this.getFileName();
        if (StringUtil.isBlank(fileName)) {
            return fileName;
        }
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    public String getTargetTableName() {
        if (this.targetTableName == null) {
            return this.getTableName();
        }
        return this.targetTableName;
    }

    public void setTargetTableName(String targetTableName) {
        this.targetTableName = targetTableName;
    }
}
