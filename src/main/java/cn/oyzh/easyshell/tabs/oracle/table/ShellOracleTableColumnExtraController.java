package cn.oyzh.easyshell.tabs.oracle.table;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.oracle.ShellOracleClient;
import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.easyshell.fx.oracle.OracleColumnKindComboBox;
import cn.oyzh.easyshell.fx.oracle.record.ShellOracleAoutoIncrementTypeComboBox;
import cn.oyzh.easyshell.fx.oracle.table.ShellOracleDefaultValueTextFiled;
import cn.oyzh.easyshell.fx.db.ShellDBEnumTextFiled;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
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
 * @since 2025-11-06
 */
public class ShellOracleTableColumnExtraController extends SubTabController {

    /**
     * 默认值组件
     */
    @FXML
    private FXHBox defaultValueBox;

    /**
     * 生成列组件
     */
    @FXML
    private FXHBox columnKindBox;

    /**
     * 字段种类
     */
    @FXML
    private OracleColumnKindComboBox columnKind;

    /**
     * 生成列表达式组件
     */
    @FXML
    private FXHBox generatedExpressionBox;

    /**
     * 生成列表达式
     */
    @FXML
    private ClearableTextField generatedExpression;

    /**
     * 默认值
     */
    @FXML
    private ShellOracleDefaultValueTextFiled defaultValue;

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
     * 自动递增类型
     */
    @FXML
    private ShellOracleAoutoIncrementTypeComboBox autoIncrementType;

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
    private OracleColumn column;

    /**
     * db客户端
     */
    private ShellOracleClient dbClient;

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
            boolean generated = this.columnKindBox.isVisible() && this.generatedKindSelected();
            this.column.setGenerated(generated);
            if (generated) {
                this.column.setGeneratedExpression(this.generatedExpression.getTextTrim());
                this.column.normalizeGeneratedAttributes();
            }
            // 自动递增处理
            if (this.autoIncrementBox.isVisible()) {
                this.column.setAutoIncrement(this.autoIncrement.isSelected());
                this.column.setAutoIncrementType(this.autoIncrementType.getSelectedItem());
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
        // 自动递增类型变化事件
        this.autoIncrementType.selectedItemChanged((observable, oldValue, newValue) -> this.apply());
        // 当前时间戳变化事件
        this.currentTimestamp.selectedChanged((observable, oldValue, newValue) -> this.apply());
        // 默认值变化事件
        this.defaultValue.addTextChangeListener((observable, oldValue, newValue) -> this.apply());
        // 字段种类变化事件
        this.columnKind.selectedItemChanged((observable, oldValue, newValue) ->
                this.onColumnKindChanged());
        // 生成列表达式变化事件
        this.generatedExpression.addTextChangeListener((observable, oldValue, newValue) -> this.apply());
        // 键长度变化事件
        this.primaryKeySize.addTextChangeListener((observable, oldValue, newValue) -> this.apply());
    }

    /**
     * 执行初始化
     *
     * @param column   字段
     * @param dbClient 客户端
     */
    public void init(OracleColumn column, ShellOracleClient dbClient) {
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

        // 生成列
        if (this.column.supportGenerated()) {
            this.columnKind.select(this.column.isGenerated()
                    ? OracleColumn.KIND_GENERATED : OracleColumn.KIND_NORMAL);
            this.columnKindBox.display();
            this.generatedExpression.text(StringUtil.blankToDefault(
                    this.column.getGeneratedExpression(), ""));
            this.updateKindLayout(this.column.isGenerated());
        } else {
            this.columnKind.clearSelection();
            this.columnKindBox.disappear();
            this.generatedExpressionBox.disappear();
        }

        // 自动递增
        if (this.column.supportAutoIncrement()) {
            this.autoIncrementBox.display();
            this.autoIncrement.setSelected(this.column.isAutoIncrement());
            if (StringUtil.isNotBlank(this.column.getAutoIncrementType())) {
                this.autoIncrementType.select(this.column.getAutoIncrementType());
            } else {
                this.autoIncrementType.selectFirst();
            }
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

    /**
     * 处理生成列状态变化，并隐藏与生成列互斥的普通列属性。
     *
     * @param generated 是否生成列
     */
    private void onColumnKindChanged() {
        if (this.ignoreChanged || this.column == null) {
            return;
        }
        boolean generated = this.generatedKindSelected();
        this.ignoreChanged = true;
        try {
            if (generated) {
                this.defaultValue.clear();
                this.autoIncrement.setSelected(false);
                this.currentTimestamp.setSelected(false);
            } else {
                this.generatedExpression.clear();
            }
            this.updateKindLayout(generated);
        } finally {
            this.ignoreChanged = false;
        }
        this.apply();
    }

    /**
     * 是否选择了生成列。
     *
     * @return 结果
     */
    private boolean generatedKindSelected() {
        return OracleColumn.KIND_GENERATED.equals(this.columnKind.getValue());
    }

    /**
     * 更新字段种类对应布局。
     *
     * @param generated 是否生成列
     */
    private void updateKindLayout(boolean generated) {
        if (generated) {
            this.generatedExpressionBox.display();
            this.defaultValueBox.disappear();
            this.autoIncrementBox.disappear();
            this.currentTimestampBox.disappear();
        } else {
            this.generatedExpressionBox.disappear();
            if (this.column != null && this.column.supportDefaultValue()) {
                this.defaultValueBox.display();
            }
            if (this.column != null && this.column.supportAutoIncrement()) {
                this.autoIncrementBox.display();
            }
            if (this.column != null && this.column.supportTimestamp()) {
                this.currentTimestampBox.display();
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        this.valueBox.managedBindVisible();
        this.unsignedBox.managedBindVisible();
        this.defaultValueBox.managedBindVisible();
        this.columnKindBox.managedBindVisible();
        this.generatedExpressionBox.managedBindVisible();
        this.autoIncrementBox.managedBindVisible();
        this.primaryKeySizeBox.managedBindVisible();
        this.currentTimestampBox.managedBindVisible();
    }
}
