package cn.oyzh.easyshell.tabs.h2.table;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.fx.h2.H2ColumnKindComboBox;
import cn.oyzh.easyshell.fx.h2.table.ShellH2DefaultValueTextFiled;
import cn.oyzh.easyshell.h2.ShellH2Client;
import cn.oyzh.easyshell.h2.column.H2Column;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.information.MessageBox;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * H2 字段扩展属性控制器。
 *
 * @since 2026-11-02
 */
public class ShellH2TableColumnExtraController extends SubTabController {

    @FXML
    private FXHBox columnKindBox;

    @FXML
    private H2ColumnKindComboBox columnKind;

    @FXML
    private FXHBox defaultValueBox;

    @FXML
    private ShellH2DefaultValueTextFiled defaultValue;

    @FXML
    private FXHBox autoIncrementBox;

    @FXML
    private FXCheckBox autoIncrement;

    @FXML
    private FXHBox checkBox;

    @FXML
    private ClearableTextField check;

    @FXML
    private FXHBox generatedExpressionBox;

    @FXML
    private ClearableTextField generatedExpression;

    private H2Column column;

    private ShellH2Client client;

    private boolean ignoreChanged;

    private final ChangeListener<String> typeListener =
            (observable, oldValue, newValue) -> this.doInit();

    private final ChangeListener<Boolean> primaryKeyListener =
            (observable, oldValue, newValue) -> this.doInit();

    /**
     * 初始化
     *
     * @param column 字段
     * @param client 客户端
     */
    public void init(H2Column column, ShellH2Client client) {
        this.ignoreChanged = true;
        try {
            if (this.column != null) {
                this.column.typeProperty().removeListener(this.typeListener);
                this.column.primaryKeyProperty().removeListener(this.primaryKeyListener);
            }
            this.column = column;
            this.client = client;
            if (this.column != null) {
                this.column.typeProperty().addListener(this.typeListener);
                this.column.primaryKeyProperty().addListener(this.primaryKeyListener);
            }
        } finally {
            this.ignoreChanged = false;
        }
        this.doInit();
    }

    private void doInit() {
        if (this.column == null) {
            this.defaultValue.clear();
            this.check.clear();
            this.columnKind.clearSelection();
            this.generatedExpression.clear();
            this.autoIncrement.setSelected(false);
            this.setBoxVisible(this.columnKindBox, false);
            this.setBoxVisible(this.autoIncrementBox, false);
            this.setBoxVisible(this.defaultValueBox, false);
            this.setBoxVisible(this.checkBox, false);
            this.setBoxVisible(this.generatedExpressionBox, false);
            return;
        }
        this.ignoreChanged = true;
        try {
            boolean generated = this.column.isGenerated();
            this.columnKind.select(generated ? H2Column.KIND_GENERATED : H2Column.KIND_NORMAL);
            this.column.setGenerated(generated);
            this.column.normalizeGeneratedAttributes();
            this.setBoxVisible(this.columnKindBox, true);
            this.updateKindLayout(generated);
            if (generated) {
                this.defaultValue.clear();
                this.autoIncrement.setSelected(false);
                this.generatedExpression.text(StringUtil.blankToDefault(
                        this.column.getGeneratedExpression(), ""));
            } else {
                boolean supportDefaultValue = this.column.supportDefaultValue();
                if (supportDefaultValue) {
                    this.defaultValue.init(this.column, this.column.getDefaultValue());
                } else {
                    this.column.setDefaultValue(null);
                    this.defaultValue.clear();
                }
                boolean supportAutoIncrement = this.column.supportAutoIncrement();
                if (supportAutoIncrement) {
                    this.autoIncrement.setSelected(this.column.isAutoIncrement());
                } else {
                    this.column.setAutoIncrement(false);
                    this.autoIncrement.setSelected(false);
                }
                this.generatedExpression.clear();
            }
            this.check.text(StringUtil.blankToDefault(this.column.getCheck(), ""));
            this.setBoxVisible(this.checkBox, true);
        } finally {
            this.ignoreChanged = false;
        }
    }

    private boolean generatedKindSelected() {
        return H2Column.KIND_GENERATED.equals(this.columnKind.getValue());
    }

    private void updateKindLayout(boolean generated) {
        this.setBoxVisible(this.generatedExpressionBox, generated);
        if (this.column != null) {
            this.setBoxVisible(this.defaultValueBox, !generated && this.column.supportDefaultValue());
            this.setBoxVisible(this.autoIncrementBox, !generated && this.column.supportAutoIncrement());
        }
    }

    private void setBoxVisible(FXHBox box, boolean visible) {
        box.setVisible(visible);
        box.setManaged(visible);
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        this.defaultValue.addTextChangeListener((observable, oldValue, newValue) -> this.apply());
        this.autoIncrement.selectedChanged((observable, oldValue, newValue) -> this.apply());
        this.columnKind.selectedItemChanged((observable, oldValue, newValue) -> this.onColumnKindChanged());
        this.check.addTextChangeListener((observable, oldValue, newValue) -> this.apply());
        this.generatedExpression.addTextChangeListener((observable, oldValue, newValue) -> this.apply());
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
            } else {
                this.generatedExpression.clear();
            }
            this.updateKindLayout(generated);
        } finally {
            this.ignoreChanged = false;
        }
        this.apply();
    }

    private void apply() {
        if (this.ignoreChanged || this.column == null) {
            return;
        }
        try {
            boolean generated = this.generatedKindSelected();
            this.column.setGenerated(generated);
            this.column.normalizeGeneratedAttributes();
            if (generated) {
                this.column.setGeneratedExpression(this.generatedExpression.getTextTrim());
            } else {
                if (this.defaultValueBox.isVisible()) {
                    this.column.setDefaultValue(this.defaultValue.getValue());
                }
                if (this.autoIncrementBox.isVisible()) {
                    this.column.setAutoIncrement(this.autoIncrement.isSelected());
                }
            }
            this.column.setCheck(this.check.getTextTrim());
            this.updateKindLayout(generated);
            this.column.setChanged(true);
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }
}
