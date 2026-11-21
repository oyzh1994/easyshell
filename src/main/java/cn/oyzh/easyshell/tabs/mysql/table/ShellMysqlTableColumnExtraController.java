package cn.oyzh.easyshell.tabs.mysql.table;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.fx.mysql.MysqlColumnKindComboBox;
import cn.oyzh.easyshell.fx.mysql.ShellMysqlCharsetComboBox;
import cn.oyzh.easyshell.fx.mysql.ShellMysqlCollationComboBox;
import cn.oyzh.easyshell.fx.mysql.table.ShellMysqlDefaultValueTextFiled;
import cn.oyzh.easyshell.fx.db.ShellDBEnumTextFiled;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.information.MessageBox;
import javafx.beans.value.ChangeListener;
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
public class ShellMysqlTableColumnExtraController extends SubTabController {

    /**
     * 默认值组件
     */
    @FXML
    private FXHBox defaultValueBox;

    /**
     * 默认值
     */
    @FXML
    private ShellMysqlDefaultValueTextFiled defaultValue;

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
     * 填充零组件
     */
    @FXML
    private FXHBox zeroFillBox;

    /**
     * 填充零
     */
    @FXML
    private FXCheckBox zeroFill;

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
     * 字符集组件
     */
    @FXML
    private FXHBox charsetBox;

    /**
     * 字符集
     */
    @FXML
    private ShellMysqlCharsetComboBox charset;

    /**
     * 排序方式组件
     */
    @FXML
    private FXHBox collationBox;

    /**
     * 排序方式
     */
    @FXML
    private ShellMysqlCollationComboBox collation;

    /**
     * 字段种类组件
     */
    @FXML
    private FXHBox columnKindBox;

    /**
     * 字段种类
     */
    @FXML
    private MysqlColumnKindComboBox columnKind;

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
     * 数据库字段
     */
    private MysqlColumn column;

    /**
     * 数据库客户端
     */
    private ShellMysqlClient dbClient;

    /**
     * 字段类型变更监听
     */
    private final ChangeListener<String> typeListener =
            (observable, oldValue, newValue) -> this.doInit();

    /**
     * 应用
     */
    private void apply() {
        try {
            if (this.ignoreChanged || this.column == null) {
                return;
            }
            boolean generated = this.generatedKindSelected();
            this.column.setGenerated(generated);
            this.column.normalizeGeneratedAttributes();
            if (generated) {
                this.column.setGeneratedExpression(this.generatedExpression.getTextTrim());
                this.column.setGeneratedStorage(StringUtil.blankToDefault(
                        this.generatedStorage.getValue(), "VIRTUAL"));
            }
            // 值处理
            if (this.valueBox.isVisible()) {
                this.column.setValue(this.value.getTextTrim());
            }
            // 字符集、排序处理
            if (this.charsetBox.isVisible()) {
                this.column.setCharset(this.charset.getValue());
                this.column.setCollation(this.collation.getValue());
            }
            // 填充零处理
            if (this.zeroFillBox.isVisible()) {
                this.column.setZeroFill(this.zeroFill.isSelected());
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
            this.updateKindLayout(generated);
            this.column.setChanged(true);
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
        // 字符集选中事件
        this.charset.selectedItemChanged((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.collation.init(newValue, this.dbClient);
                this.collation.selectFirst();
            }
            this.apply();
        });
        // 填充零变化事件
        this.zeroFill.selectedChanged((observable, oldValue, newValue) -> this.apply());
        // 无符号变化事件
        this.unsigned.selectedChanged((observable, oldValue, newValue) -> this.apply());
        // 值变化事件
        this.value.addTextChangeListener((observable, oldValue, newValue) -> this.apply());
        // 排序选中事件
        this.collation.selectedItemChanged((observable, oldValue, newValue) -> this.apply());
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
    public void init(MysqlColumn column, ShellMysqlClient dbClient) {
        // 移除旧的监听器
        if (this.column != null) {
            this.column.typeProperty().removeListener(this.typeListener);
        }
        this.column = column;
        this.dbClient = dbClient;
        this.doInit();
        if (column != null) {
            column.typeProperty().addListener(this.typeListener);
        }
    }

    /**
     * 执行初始化
     */
    public void doInit() {
        this.ignoreChanged = true;
        if (this.column == null) {
            this.columnKind.clearSelection();
            this.generatedExpression.clear();
            this.generatedStorage.clearSelection();
            this.defaultValue.clear();
            this.autoIncrement.setSelected(false);
            this.currentTimestamp.setSelected(false);
            this.valueBox.disappear();
            this.defaultValueBox.disappear();
            this.autoIncrementBox.disappear();
            this.zeroFillBox.disappear();
            this.unsignedBox.disappear();
            this.charsetBox.disappear();
            this.collationBox.disappear();
            this.primaryKeySizeBox.disappear();
            this.currentTimestampBox.disappear();
            this.columnKindBox.disappear();
            this.generatedExpressionBox.disappear();
            this.generatedStorageBox.disappear();
            this.ignoreChanged = false;
            return;
        }
        boolean generated = this.column.isGenerated();
        this.columnKind.select(generated ? MysqlColumn.KIND_GENERATED : MysqlColumn.KIND_NORMAL);
        this.columnKindBox.display();
        this.generatedStorage.setItem(List.of("VIRTUAL", "STORED"));
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
        if (!generated && this.column.supportAutoIncrement()) {
            this.autoIncrementBox.display();
            this.autoIncrement.setSelected(this.column.isAutoIncrement());
        } else {
            this.autoIncrementBox.disappear();
        }

        // 填充零
        if (this.column.supportZeroFill()) {
            this.zeroFillBox.display();
            this.zeroFill.setSelected(this.column.isZeroFill());
        } else {
            this.zeroFillBox.disappear();
        }

        // 无符号
        if (this.column.supportUnsigned()) {
            this.unsignedBox.display();
            this.unsigned.setSelected(this.column.isUnsigned());
        } else {
            this.unsignedBox.disappear();
        }

        // 字符集及排序
        if (this.column.supportCharset()) {
            this.charsetBox.display();
            this.collationBox.display();
            if (this.charset.isItemEmpty()) {
                this.charset.init(this.dbClient);
            }
            this.charset.select(this.column.getCharset());
            this.collation.select(this.column.getCollation());
        } else {
            this.charsetBox.disappear();
            this.collationBox.disappear();
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
     * 是否选择了生成列。
     *
     * @return 结果
     */
    private boolean generatedKindSelected() {
        return MysqlColumn.KIND_GENERATED.equals(this.columnKind.getValue());
    }

    /**
     * 更新字段种类对应布局。
     *
     * @param generated 是否生成列
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

    /**
     * 字段种类变更。
     */
    private void onColumnKindChanged() {
        if (this.ignoreChanged || this.column == null) {
            return;
        }
        boolean generated = this.generatedKindSelected();
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

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        this.valueBox.managedBindVisible();
        this.charsetBox.managedBindVisible();
        this.unsignedBox.managedBindVisible();
        this.zeroFillBox.managedBindVisible();
        this.collationBox.managedBindVisible();
        this.defaultValueBox.managedBindVisible();
        this.autoIncrementBox.managedBindVisible();
        this.primaryKeySizeBox.managedBindVisible();
        this.currentTimestampBox.managedBindVisible();
        this.columnKindBox.managedBindVisible();
        this.generatedExpressionBox.managedBindVisible();
        this.generatedStorageBox.managedBindVisible();
    }
}
