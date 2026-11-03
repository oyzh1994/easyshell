package cn.oyzh.easyshell.tabs.sqlite.table;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.fx.sqlite.SQLiteCollationComboBox;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.information.MessageBox;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * SQLite 字段扩展属性控制器。
 *
 * @since 2026-11-02
 */
public class ShellSqliteTableColumnExtraController extends SubTabController {

    @FXML
    private FXHBox autoIncrementBox;

    @FXML
    private FXCheckBox autoIncrement;

    @FXML
    private FXHBox collateBox;

    @FXML
    private SQLiteCollationComboBox collate;

    @FXML
    private ClearableTextField check;

    @FXML
    private FXHBox generatedExpressionBox;

    @FXML
    private FXHBox generatedStorageBox;

    @FXML
    private ClearableTextField generatedExpression;

    @FXML
    private FXComboBox<String> generatedStorage;

    private SQLiteColumn column;

    private ShellSqliteClient client;

    private boolean ignoreChanged;

    private final ChangeListener<String> typeListener =
            (observable, oldValue, newValue) -> this.doInit();

    private final ChangeListener<Boolean> primaryKeyListener =
            (observable, oldValue, newValue) -> this.doInit();

    public void init(SQLiteColumn column, ShellSqliteClient client) {
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
            this.collate.init(this.client, this.column == null ? null : this.column.getCollate());
        } finally {
            this.ignoreChanged = false;
        }
        this.doInit();
    }

    private void doInit() {
        this.ignoreChanged = true;
        try {
            boolean available = this.column != null;
            boolean supportAutoIncrement = available
                    && this.column.isPrimaryKey()
                    && this.column.supportAutoIncrement();
            this.autoIncrementBox.setVisible(supportAutoIncrement);
            this.autoIncrementBox.setManaged(supportAutoIncrement);
            this.collateBox.setVisible(available);
            this.collateBox.setManaged(available);
            this.generatedExpressionBox.setVisible(available);
            this.generatedExpressionBox.setManaged(available);
            this.generatedStorageBox.setVisible(available);
            this.generatedStorageBox.setManaged(available);
            if (!available) {
                this.autoIncrement.setSelected(false);
                this.collate.select("");
                this.check.clear();
                this.generatedExpression.clear();
                return;
            }
            if (supportAutoIncrement) {
                this.autoIncrement.setSelected(this.column.isAutoIncrement());
            } else {
                this.autoIncrement.setSelected(false);
                this.column.setAutoIncrement(false);
            }
            this.collate.select(StringUtil.blankToDefault(this.column.getCollate(), ""));
            this.check.setText(StringUtil.blankToDefault(this.column.getCheck(), ""));
            this.generatedExpression.setText(StringUtil.blankToDefault(this.column.getGeneratedExpression(), ""));
            this.generatedStorage.getItems().setAll("VIRTUAL", "STORED");
            this.generatedStorage.setValue(StringUtil.blankToDefault(this.column.getGeneratedStorage(), "VIRTUAL"));
        } finally {
            this.ignoreChanged = false;
        }
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        this.autoIncrement.selectedChanged((observable, oldValue, newValue) -> this.apply());
        this.collate.selectedItemChanged((observable, oldValue, newValue) -> this.apply());
        this.check.addTextChangeListener((observable, oldValue, newValue) -> this.apply());
        this.generatedExpression.addTextChangeListener((observable, oldValue, newValue) -> this.apply());
        this.generatedStorage.valueProperty().addListener((observable, oldValue, newValue) -> this.apply());
    }

    private void apply() {
        if (this.ignoreChanged || this.column == null) {
            return;
        }
        try {
            if (this.autoIncrementBox.isVisible()) {
                this.column.setAutoIncrement(this.autoIncrement.isSelected());
            }
            this.column.setCollate(StringUtil.blankToDefault(this.collate.getSelectedItem(), null));
            this.column.setCheck(this.check.getTextTrim());
            this.column.setGeneratedExpression(this.generatedExpression.getTextTrim());
            this.column.setGeneratedStorage(this.generatedStorage.getValue());
            this.column.setChanged(true);
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        this.autoIncrementBox.setVisible(false);
        this.autoIncrementBox.setManaged(false);
        this.collateBox.setVisible(false);
        this.collateBox.setManaged(false);
        this.generatedExpressionBox.setVisible(false);
        this.generatedExpressionBox.setManaged(false);
        this.generatedStorageBox.setVisible(false);
        this.generatedStorageBox.setManaged(false);
    }
}
