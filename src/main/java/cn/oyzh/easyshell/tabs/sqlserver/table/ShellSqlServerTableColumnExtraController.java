package cn.oyzh.easyshell.tabs.sqlserver.table;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlserver.ShellSqlServerClient;
import cn.oyzh.easyshell.sqlserver.column.SqlServerColumn;
import cn.oyzh.easyshell.fx.sqlserver.table.ShellSqlServerDefaultValueTextFiled;
import cn.oyzh.easyshell.fx.sqlserver.table.SqlServerColumnKindComboBox;
import cn.oyzh.easyshell.fx.db.ShellDBEnumTextFiled;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.information.MessageBox;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * 字段配置弹窗
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellSqlServerTableColumnExtraController extends SubTabController {

    /**
     * 字段种类组件
     */
    @FXML
    private FXHBox columnKindBox;

    /**
     * 字段种类
     */
    @FXML
    private SqlServerColumnKindComboBox columnKind;

    /**
     * 生成表达式组件
     */
    @FXML
    private FXHBox generatedExpressionBox;

    /**
     * 生成表达式
     */
    @FXML
    private ClearableTextField generatedExpression;

    /**
     * 生成列存储方式组件
     */
    @FXML
    private FXHBox generatedStorageBox;

    /**
     * 生成列存储方式
     */
    @FXML
    private FXComboBox<String> generatedStorage;

    /**
     * 默认值组件
     */
    @FXML
    private FXHBox defaultValueBox;

    /**
     * 默认值
     */
    @FXML
    private ShellSqlServerDefaultValueTextFiled defaultValue;

    /**
     * 字段值组件
     */
    @FXML
    private FXHBox valueBox;

    /**
     * 字段值
     */
    @FXML
    private ShellDBEnumTextFiled value;

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
    private SqlServerColumn column;

    /**
     * db客户端
     */
    private ShellSqlServerClient dbClient;

    /**
     * 应用
     */
    private void apply() {
        try {
            if (this.ignoreChanged) {
                return;
            }
            boolean generated = SqlServerColumn.KIND_GENERATED.equals(this.columnKind.getValue());
            this.column.setGenerated(generated);
            if (generated) {
                this.column.setGeneratedExpression(this.generatedExpression.getTextTrim());
                this.column.setGeneratedStorage(StringUtil.blankToDefault(
                        this.generatedStorage.getValue(), "VIRTUAL"));
            } else if (this.column.getGeneratedExpression() != null) {
                this.column.setGeneratedExpression(null);
                this.column.setGeneratedStorage(null);
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
                this.column.setAutoIncrementType(
                        this.autoIncrement.isSelected() ? "IDENTITY" : null);
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
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 忽略变更标志
     */
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
        // 字段种类变更事件
        this.columnKind.selectedItemChanged((observable, oldValue, newValue) -> this.onColumnKindChanged());
        // 生成表达式变更事件
        this.generatedExpression.addTextChangeListener((observable, oldValue, newValue) -> this.apply());
        // 生成列存储方式变更事件
        this.generatedStorage.valueProperty().addListener((observable, oldValue, newValue) -> this.apply());
    }

    /**
     * 执行初始化
     *
     * @param column   字段
     * @param dbClient 客户端
     */
    public void init(SqlServerColumn column, ShellSqlServerClient dbClient) {
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

    /**
     * 监听列类型变更
     *
     * @param observableValue 可观察值
     * @param s               参数
     * @param s1              参数
     */
    private void listenColumnTypeChanged(ObservableValue<? extends String> observableValue, String s, String s1) {
        this.doInit();
    }

    /**
     * 执行初始化
     */
    public void doInit() {
        this.ignoreChanged = true;
        boolean generated = this.column.isGenerated();
        this.columnKind.select(generated ? SqlServerColumn.KIND_GENERATED : SqlServerColumn.KIND_NORMAL);
        this.columnKindBox.display();
        this.generatedStorage.setItem(List.of("VIRTUAL", "PERSISTED"));
        this.generatedExpression.text(StringUtil.blankToDefault(
                this.column.getGeneratedExpression(), ""));
        this.generatedStorage.setValue(StringUtil.blankToDefault(
                this.column.getGeneratedStorage(), "VIRTUAL"));
        // 值
        if (this.column.supportValue()) {
            this.valueBox.display();
            this.value.setValues(this.column.getValueList());
        } else {
            this.valueBox.disappear();
        }

        // 默认值
        if (!generated && this.column.supportDefaultValue()) {
            this.defaultValueBox.display();
            this.defaultValue.init(this.column, this.column.getDefaultValueString());
        } else {
            this.defaultValueBox.disappear();
        }

        // 自动递增
        if (!generated && this.column.supportAutoIncrement()
                && (this.column.isCreated() || this.column.isAutoIncrement())) {
            this.autoIncrementBox.display();
            this.autoIncrement.setSelected(this.column.isAutoIncrement());
            // SQL Server不支持给既有普通列追加或移除IDENTITY
            this.autoIncrement.setDisable(!this.column.isCreated());
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
        if (!generated && this.column.supportTimestamp()) {
            this.currentTimestampBox.display();
            this.currentTimestamp.setSelected(this.column.isUpdateOnCurrentTimestamp());
        } else {
            this.currentTimestampBox.disappear();
        }
        this.updateKindLayout(generated);
        this.ignoreChanged = false;
    }

    /**
     * 字段种类变更。
     */
    private void onColumnKindChanged() {
        if (this.ignoreChanged || this.column == null) {
            return;
        }
        boolean generated = SqlServerColumn.KIND_GENERATED.equals(this.columnKind.getValue());
        this.ignoreChanged = true;
        try {
            if (generated) {
                this.generatedStorage.setValue("VIRTUAL");
                this.defaultValue.clear();
                this.autoIncrement.setSelected(false);
                this.currentTimestamp.setSelected(false);
            } else {
                this.generatedExpression.clear();
                this.generatedStorage.clearSelection();
            }
            this.updateKindLayout(generated);
        } finally {
            this.ignoreChanged = false;
        }
        this.apply();
    }

    /**
     * 更新字段种类对应布局。
     */
    private void updateKindLayout(boolean generated) {
        if (generated) {
            this.defaultValueBox.disappear();
            this.autoIncrementBox.disappear();
            this.currentTimestampBox.disappear();
            this.generatedExpressionBox.display();
            this.generatedStorageBox.display();
        } else {
            if (this.column.supportDefaultValue()) {
                this.defaultValueBox.display();
            }
            if (this.column.supportAutoIncrement()) {
                this.autoIncrementBox.display();
            }
            if (this.column.supportTimestamp()) {
                this.currentTimestampBox.display();
            }
            this.generatedExpressionBox.disappear();
            this.generatedStorageBox.disappear();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        this.valueBox.managedBindVisible();
        this.unsignedBox.managedBindVisible();
        this.defaultValueBox.managedBindVisible();
        this.autoIncrementBox.managedBindVisible();
        this.primaryKeySizeBox.managedBindVisible();
        this.currentTimestampBox.managedBindVisible();
        this.columnKindBox.managedBindVisible();
        this.generatedExpressionBox.managedBindVisible();
        this.generatedStorageBox.managedBindVisible();
    }
}
