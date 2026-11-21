package cn.oyzh.easyshell.tabs.mariadb.table;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.fx.mariadb.MariadbColumnKindComboBox;
import cn.oyzh.easyshell.fx.mariadb.ShellMariadbCharsetComboBox;
import cn.oyzh.easyshell.fx.mariadb.ShellMariadbCollationComboBox;
import cn.oyzh.easyshell.fx.mariadb.table.ShellMariadbDefaultValueTextFiled;
import cn.oyzh.easyshell.fx.db.ShellDBEnumTextFiled;
import cn.oyzh.easyshell.mariadb.ShellMariadbClient;
import cn.oyzh.easyshell.mariadb.column.MariadbColumn;
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
 * MariaDB 字段配置弹窗。
 */
public class ShellMariadbTableColumnExtraController extends SubTabController {

    @FXML
    private FXHBox defaultValueBox;

    @FXML
    private ShellMariadbDefaultValueTextFiled defaultValue;

    @FXML
    private FXHBox valueBox;

    @FXML
    private ShellDBEnumTextFiled value;

    @FXML
    private FXHBox primaryKeySizeBox;

    @FXML
    private NumberTextField primaryKeySize;

    @FXML
    private FXHBox zeroFillBox;

    @FXML
    private FXCheckBox zeroFill;

    @FXML
    private FXHBox autoIncrementBox;

    @FXML
    private FXCheckBox autoIncrement;

    @FXML
    private FXHBox unsignedBox;

    @FXML
    private FXCheckBox unsigned;

    @FXML
    private FXHBox currentTimestampBox;

    @FXML
    private FXCheckBox currentTimestamp;

    @FXML
    private FXHBox charsetBox;

    @FXML
    private ShellMariadbCharsetComboBox charset;

    @FXML
    private FXHBox collationBox;

    @FXML
    private ShellMariadbCollationComboBox collation;

    @FXML
    private FXHBox columnKindBox;

    @FXML
    private MariadbColumnKindComboBox columnKind;

    @FXML
    private FXHBox generatedExpressionBox;

    @FXML
    private ClearableTextField generatedExpression;

    @FXML
    private FXHBox generatedStorageBox;

    @FXML
    private FXComboBox<String> generatedStorage;

    private MariadbColumn column;

    private ShellMariadbClient dbClient;

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
                this.column.setGeneratedStorage(StringUtil.blankToDefault(
                        this.generatedStorage.getValue(), "VIRTUAL"));
            }
            if (this.valueBox.isVisible()) {
                this.column.setValue(this.value.getTextTrim());
            }
            if (this.charsetBox.isVisible()) {
                this.column.setCharset(this.charset.getValue());
                this.column.setCollation(this.collation.getValue());
            }
            if (this.zeroFillBox.isVisible()) {
                this.column.setZeroFill(this.zeroFill.isSelected());
            }
            if (this.unsignedBox.isVisible()) {
                this.column.setUnsigned(this.unsigned.isSelected());
            }
            if (this.defaultValueBox.isVisible()) {
                this.column.setDefaultValue(this.defaultValue.getValue());
            }
            if (this.autoIncrementBox.isVisible()) {
                this.column.setAutoIncrement(this.autoIncrement.isSelected());
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
        this.charset.selectedItemChanged((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.collation.init(newValue, this.dbClient);
                this.collation.selectFirst();
            }
            this.apply();
        });
        this.zeroFill.selectedChanged((observable, oldValue, newValue) -> this.apply());
        this.unsigned.selectedChanged((observable, oldValue, newValue) -> this.apply());
        this.value.addTextChangeListener((observable, oldValue, newValue) -> this.apply());
        this.collation.selectedItemChanged((observable, oldValue, newValue) -> this.apply());
        this.autoIncrement.selectedChanged((observable, oldValue, newValue) -> this.apply());
        this.currentTimestamp.selectedChanged((observable, oldValue, newValue) -> this.apply());
        this.defaultValue.addTextChangeListener((observable, oldValue, newValue) -> this.apply());
        this.primaryKeySize.addTextChangeListener((observable, oldValue, newValue) -> this.apply());
        this.columnKind.selectedItemChanged((observable, oldValue, newValue) -> this.onColumnKindChanged());
        this.generatedExpression.addTextChangeListener((observable, oldValue, newValue) -> this.apply());
        this.generatedStorage.valueProperty().addListener((observable, oldValue, newValue) -> this.apply());
    }

    /**
     * 初始化字段配置。
     *
     * @param column   字段
     * @param dbClient 客户端
     */
    public void init(MariadbColumn column, ShellMariadbClient dbClient) {
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

    private void doInit() {
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
        this.columnKind.select(generated ? MariadbColumn.KIND_GENERATED : MariadbColumn.KIND_NORMAL);
        this.columnKindBox.display();
        this.generatedStorage.setItem(List.of("VIRTUAL", "STORED"));
        this.generatedExpression.text(StringUtil.blankToDefault(
                this.column.getGeneratedExpression(), ""));
        this.generatedStorage.setValue(StringUtil.blankToDefault(
                this.column.getGeneratedStorage(), "VIRTUAL"));
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
        } else {
            this.autoIncrementBox.disappear();
        }
        if (this.column.supportZeroFill()) {
            this.zeroFillBox.display();
            this.zeroFill.setSelected(this.column.isZeroFill());
        } else {
            this.zeroFillBox.disappear();
        }
        if (this.column.supportUnsigned()) {
            this.unsignedBox.display();
            this.unsigned.setSelected(this.column.isUnsigned());
        } else {
            this.unsignedBox.disappear();
        }
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
        return MariadbColumn.KIND_GENERATED.equals(this.columnKind.getValue());
    }

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
