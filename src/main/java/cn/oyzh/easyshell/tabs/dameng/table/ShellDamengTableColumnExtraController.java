package cn.oyzh.easyshell.tabs.dameng.table;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.fx.dameng.DamengColumnKindComboBox;
import cn.oyzh.easyshell.fx.dameng.record.ShellDamengAoutoIncrementTypeComboBox;
import cn.oyzh.easyshell.fx.dameng.table.ShellDamengDefaultValueTextFiled;
import cn.oyzh.easyshell.fx.db.ShellDBEnumTextFiled;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.information.MessageBox;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * 达梦字段配置弹窗。
 */
public class ShellDamengTableColumnExtraController extends SubTabController {

    @FXML
    private FXHBox defaultValueBox;

    @FXML
    private ShellDamengDefaultValueTextFiled defaultValue;

    @FXML
    private FXHBox valueBox;

    @FXML
    private ShellDBEnumTextFiled value;

    @FXML
    private FXHBox primaryKeySizeBox;

    @FXML
    private NumberTextField primaryKeySize;

    @FXML
    private FXHBox autoIncrementBox;

    @FXML
    private FXCheckBox autoIncrement;

    @FXML
    private ShellDamengAoutoIncrementTypeComboBox autoIncrementType;

    @FXML
    private FXHBox unsignedBox;

    @FXML
    private FXCheckBox unsigned;

    @FXML
    private FXHBox currentTimestampBox;

    @FXML
    private FXCheckBox currentTimestamp;

    @FXML
    private FXHBox columnKindBox;

    @FXML
    private DamengColumnKindComboBox columnKind;

    @FXML
    private FXHBox generatedExpressionBox;

    @FXML
    private ClearableTextField generatedExpression;

    private DamengColumn column;

    private ShellDamengClient dbClient;

    private final ChangeListener<String> typeListener =
            (observable, oldValue, newValue) -> this.doInit();

    private boolean ignoreChanged;

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
            }
            if (this.valueBox.isVisible()) {
                this.column.setValue(this.value.getTextTrim());
            }
            if (this.unsignedBox.isVisible()) {
                this.column.setUnsigned(this.unsigned.isSelected());
            }
            if (this.defaultValueBox.isVisible()) {
                this.column.setDefaultValue(this.defaultValue.getValue());
            }
            if (this.autoIncrementBox.isVisible()) {
                this.column.setAutoIncrement(this.autoIncrement.isSelected());
                this.column.setAutoIncrementType(this.autoIncrementType.getSelectedItem());
            }
            if (this.primaryKeySizeBox.isVisible()) {
                this.column.setPrimaryKeySize(this.primaryKeySize.getIntValue());
            }
            if (this.currentTimestampBox.isVisible()) {
                this.column.setUpdateOnCurrentTimestamp(this.currentTimestamp.isSelected());
            }
            this.updateKindLayout(generated);
            this.column.setChanged(true);
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        this.unsigned.selectedChanged((observable, oldValue, newValue) -> this.apply());
        this.value.addTextChangeListener((observable, oldValue, newValue) -> this.apply());
        this.autoIncrement.selectedChanged((observable, oldValue, newValue) -> this.apply());
        this.autoIncrementType.selectedItemChanged((observable, oldValue, newValue) -> this.apply());
        this.currentTimestamp.selectedChanged((observable, oldValue, newValue) -> this.apply());
        this.defaultValue.addTextChangeListener((observable, oldValue, newValue) -> this.apply());
        this.primaryKeySize.addTextChangeListener((observable, oldValue, newValue) -> this.apply());
        this.columnKind.selectedItemChanged((observable, oldValue, newValue) -> this.onColumnKindChanged());
        this.generatedExpression.addTextChangeListener((observable, oldValue, newValue) -> this.apply());
    }

    /**
     * 初始化字段配置。
     *
     * @param column   字段
     * @param dbClient 客户端
     */
    public void init(DamengColumn column, ShellDamengClient dbClient) {
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

    public void doInit() {
        this.ignoreChanged = true;
        if (this.column == null) {
            this.columnKind.clearSelection();
            this.generatedExpression.clear();
            this.defaultValue.clear();
            this.autoIncrement.setSelected(false);
            this.currentTimestamp.setSelected(false);
            this.valueBox.disappear();
            this.defaultValueBox.disappear();
            this.autoIncrementBox.disappear();
            this.unsignedBox.disappear();
            this.primaryKeySizeBox.disappear();
            this.currentTimestampBox.disappear();
            this.columnKindBox.disappear();
            this.generatedExpressionBox.disappear();
            this.ignoreChanged = false;
            return;
        }
        boolean generated = this.column.isGenerated();
        this.columnKind.select(generated ? DamengColumn.KIND_GENERATED : DamengColumn.KIND_NORMAL);
        this.columnKindBox.display();
        this.generatedExpression.text(StringUtil.blankToDefault(
                this.column.getGeneratedExpression(), ""));
        if (this.column.supportValue()) {
            this.valueBox.display();
            this.value.setValues(this.column.getValueList());
        } else {
            this.valueBox.disappear();
        }
        if (!generated && this.column.supportDefaultValue()) {
            this.defaultValueBox.display();
            this.defaultValue.init(this.column, this.column.getDefaultValueString());
        } else {
            this.defaultValueBox.disappear();
        }
        if (!generated && this.column.supportAutoIncrement()) {
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
        if (this.column.supportUnsigned()) {
            this.unsignedBox.display();
            this.unsigned.setSelected(this.column.isUnsigned());
        } else {
            this.unsignedBox.disappear();
        }
        if (this.column.isPrimaryKey() && this.column.supportKeySize()) {
            this.primaryKeySizeBox.display();
            if (this.column.getPrimaryKeySize() != null) {
                this.primaryKeySize.setValue(this.column.getPrimaryKeySize());
            }
        } else {
            this.primaryKeySizeBox.disappear();
        }
        if (!generated && this.column.supportTimestamp()) {
            this.currentTimestampBox.display();
            this.currentTimestamp.setSelected(this.column.isUpdateOnCurrentTimestamp());
        } else {
            this.currentTimestampBox.disappear();
        }
        this.updateKindLayout(generated);
        this.ignoreChanged = false;
    }

    private boolean generatedKindSelected() {
        return DamengColumn.KIND_GENERATED.equals(this.columnKind.getValue());
    }

    private void updateKindLayout(boolean generated) {
        if (generated) {
            this.defaultValueBox.disappear();
            this.autoIncrementBox.disappear();
            this.currentTimestampBox.disappear();
            this.generatedExpressionBox.display();
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
        }
    }

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
    }
}
