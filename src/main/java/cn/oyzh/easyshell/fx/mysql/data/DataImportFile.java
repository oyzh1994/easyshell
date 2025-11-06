package cn.oyzh.easyshell.fx.mysql.data;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.gui.text.field.ChooseFileTextField;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.File;

/**
 * @author oyzh
 * @since 2024/08/30
 */
public class DataImportFile {

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

    public String getTableName() {
        String fileName = this.getFileName();
        if (StringUtil.isBlank(fileName)) {
            return fileName;
        }
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    public String getTargetTableName() {
        if(this.targetTableName == null) {
            return this.getTableName();
        }
        return this.targetTableName;
    }

    public void setTargetTableName(String targetTableName) {
        this.targetTableName = targetTableName;
    }
}
