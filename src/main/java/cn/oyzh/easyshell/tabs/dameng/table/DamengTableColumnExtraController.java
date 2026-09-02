package cn.oyzh.easyshell.tabs.dameng.table;

import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.fx.dameng.table.DBEnumTextFiled;
import cn.oyzh.easyshell.fx.dameng.table.DamengDefaultValueTextFiled;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.information.MessageBox;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * 字段配置弹窗
 *
 * @author oyzh
 * @since 2024/07/12
 */
public class DamengTableColumnExtraController extends SubTabController {

    /**
     * 默认值组件
     */
    @FXML
    private FXHBox defaultValueBox;

    /**
     * 默认值
     */
    @FXML
    private DamengDefaultValueTextFiled defaultValue;

    /**
     * 字段值组件
     */
    @FXML
    private FXHBox valueBox;

    /**
     * 字段值
     */
    @FXML
    private DBEnumTextFiled value;

    /**
     * 主键长度组件
     */
    @FXML
    private FXHBox primaryKeySizeBox;

    /**
     * 主键长度
     */
    @FXML
    private NumberTextField primaryKeySize;

    /**
     * 自动递增组件
     */
    @FXML
    private FXHBox autoIncrementBox;

    /**
     * 自动递增
     */
    @FXML
    private FXCheckBox autoIncrement;

    /**
     * 无符号组件
     */
    @FXML
    private FXHBox unsignedBox;

    /**
     * 无符号
     */
    @FXML
    private FXCheckBox unsigned;

    /**
     * 根据当前时间戳更新组件
     */
    @FXML
    private FXHBox currentTimestampBox;

    /**
     * 根据当前时间戳更新
     */
    @FXML
    private FXCheckBox currentTimestamp;

    /**
     * db字段
     */
    private DamengColumn column;

    /**
     * db客户端
     */
    private ShellDamengClient dbClient;

    /**
     * 应用
     */
    private void apply() {
        try {
            if (this.ignoreChanged) {
                return;
            }
            // 值处理
            if (this.valueBox.isVisible()) {
                this.column.setValue(this.value.getTextTrim());
            }
            // 无符号处理
            if (this.unsignedBox.isVisible()) {
                this.column.setUnsigned(this.unsigned.isSelected());
            }
            // 默认值处理
            if (this.defaultValueBox.isVisible()) {
                this.column.setDefaultValue(this.defaultValue.getValue());
            }
            // 自动递增处理
            if (this.autoIncrementBox.isVisible()) {
                this.column.setAutoIncrement(this.autoIncrement.isSelected());
            }
            // 主键长度处理
            if (this.primaryKeySizeBox.isVisible()) {
                this.column.setPrimaryKeySize(this.primaryKeySize.getIntValue());
            }
            // 根据时间戳更新处理
            if (this.currentTimestampBox.isVisible()) {
                this.column.setUpdateOnCurrentTimestamp(this.currentTimestamp.isSelected());
            }
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    private boolean ignoreChanged = false;

    @Override
    protected void bindListeners() {
        super.bindListeners();
        // 无符号变化事件
        this.unsigned.selectedChanged((observable, oldValue, newValue) -> this.apply());
        // 值变化事件
        this.value.addTextChangeListener((observable, oldValue, newValue) -> this.apply());
        // 自动递增变化事件
        this.autoIncrement.selectedChanged((observable, oldValue, newValue) -> this.apply());
        // 当前时间戳变化事件
        this.currentTimestamp.selectedChanged((observable, oldValue, newValue) -> this.apply());
        // 默认值变化事件
        this.defaultValue.addTextChangeListener((observable, oldValue, newValue) -> this.apply());
        // 键长度变化事件
        this.primaryKeySize.addTextChangeListener((observable, oldValue, newValue) -> this.apply());
    }

    /**
     * 执行初始化
     *
     * @param column   字段
     * @param dbClient 客户端
     */
    public void init(DamengColumn column, ShellDamengClient dbClient) {
        // 移除旧的监听器
        if (this.column != null) {
            this.column.typeProperty().removeListener(this::listenColumnTypeChanged);
        }
        if (column == null) {
            return;
        }
        this.column = column;
        this.dbClient = dbClient;
        this.doInit();
        column.typeProperty().addListener(this::listenColumnTypeChanged);
    }

    private void listenColumnTypeChanged(ObservableValue<? extends String> observableValue, String s, String s1) {
        this.doInit();
    }

    public void doInit() {
        this.ignoreChanged = true;
        // 值
        if (this.column.supportValue()) {
            this.valueBox.display();
            this.value.setValues(this.column.getValueList());
        } else {
            this.valueBox.disappear();
        }

        // 默认值
        if (this.column.supportDefaultValue()) {
            this.defaultValueBox.display();
            this.defaultValue.init(this.column, this.column.getDefaultValueString());
        } else {
            this.defaultValueBox.disappear();
        }

        // 自动递增
        if (this.column.supportAutoIncrement()) {
            this.autoIncrementBox.display();
            this.autoIncrement.setSelected(this.column.isAutoIncrement());
        } else {
            this.autoIncrementBox.disappear();
        }

        // 无符号
        if (this.column.supportUnsigned()) {
            this.unsignedBox.display();
            this.unsigned.setSelected(this.column.isUnsigned());
        } else {
            this.unsignedBox.disappear();
        }

        // 主键长度
        if (this.column.isPrimaryKey() && this.column.supportKeySize()) {
            this.primaryKeySizeBox.display();
            if (this.column.getPrimaryKeySize() != null) {
                this.primaryKeySize.setValue(this.column.getPrimaryKeySize());
            }
        } else {
            this.primaryKeySizeBox.disappear();
        }

        // 根据时间戳更新
        if (this.column.supportTimestamp()) {
            this.currentTimestampBox.display();
            this.currentTimestamp.setSelected(this.column.isUpdateOnCurrentTimestamp());
        } else {
            this.currentTimestampBox.disappear();
        }
        this.ignoreChanged = false;
    }

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        this.valueBox.managedBindVisible();
        this.unsignedBox.managedBindVisible();
        this.defaultValueBox.managedBindVisible();
        this.autoIncrementBox.managedBindVisible();
        this.primaryKeySizeBox.managedBindVisible();
        this.currentTimestampBox.managedBindVisible();
    }
}
