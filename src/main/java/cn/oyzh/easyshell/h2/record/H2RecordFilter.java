package cn.oyzh.easyshell.h2.record;

import cn.oyzh.easyshell.h2.column.H2Column;
import cn.oyzh.easyshell.h2.condition.H2Condition;
import cn.oyzh.easyshell.h2.condition.H2ConditionUtil;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBRecordFilter;
import cn.oyzh.fx.db.condition.ui.DBConditionComboBox;
import cn.oyzh.fx.db.ui.DBColumnComboBox;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.flex.FlexUtil;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Node;
import javafx.scene.control.TextField;

import java.util.List;

/**
 * H2 记录过滤条件。
 *
 * @since 2026-11-02
 */
public class H2RecordFilter extends DBRecordFilter {

    private H2Condition condition;

    @Override
    public Object value() throws Exception {
        if (this.valueBox == null) {
            return this.value;
        }
        if (this.valueBox.isChildEmpty() || this.valueBox.isDisable()) {
            return this.condition == null ? this.value : this.condition.getValue();
        }
        return this.value = H2ConditionUtil.getNodeVal(this.valueBox.getChildren());
    }

    public Node getValueControl() {
        this.updateValueControl();
        return this.valueBox;
    }

    @Override
    protected void updateValueControl() {
        if (this.condition != null && !this.condition.isRequireCondition()) {
            if (this.valueBox == null) {
                this.valueBox = new FXHBox();
                FlexUtil.flexWidth(this.valueBox, "100%");
            }
            NodeUtil.disable(this.valueBox);
            return;
        }
        if (this.valueBox == null) {
            this.valueBox = new FXHBox();
            FlexUtil.flexWidth(this.valueBox, "100%");
        }
        NodeUtil.enable(this.valueBox);
        List<Node> nodes = H2ConditionUtil.generateNode((H2Column) this.column, this.condition);
        H2ConditionUtil.setNodeVal(nodes, this.value);
        if (nodes.size() == 1) {
            FlexUtil.flexWidth(nodes.getFirst(), "100% - 10");
            FlexUtil.flexHeight(nodes.getFirst(), "100%");
        } else if (nodes.size() == 2) {
            FlexUtil.flexWidth(nodes.get(0), "50% - 10");
            FlexUtil.flexHeight(nodes.get(0), "100%");
            FlexUtil.flexWidth(nodes.get(1), "50% - 10");
            FlexUtil.flexHeight(nodes.get(1), "100%");
        }
        for (Node node : nodes) {
            if (node instanceof TextField textField) {
                textField.setPromptText(I18nHelper.pleaseInputContent());
            }
            TableViewUtil.selectRowOnMouseClicked(node);
        }
        this.valueBox.setChild(nodes);
    }

    public DBColumnComboBox getColumnControl() {
        DBColumnComboBox comboBox = new DBColumnComboBox(this.columns);
        comboBox.selectedItemChanged((observable, oldValue, newValue) -> {
            this.column = newValue;
            this.updateValueControl();
        });
        comboBox.selectFirstIfNull(this.column);
        TableViewUtil.selectRowOnMouseClicked(comboBox);
        return comboBox;
    }

    public DBConditionComboBox getConditionControl() {
        H2ConditionUtil.ensureRegistered();
        DBConditionComboBox comboBox = new DBConditionComboBox(DBDialect.H2);
        comboBox.selectedItemChanged((observable, oldValue, newValue) -> {
            this.condition = (H2Condition) newValue;
            this.updateValueControl();
        });
        comboBox.selectFirstIfNull(this.condition);
        TableViewUtil.selectRowOnMouseClicked(comboBox);
        return comboBox;
    }

    public String column() {
        return this.column.getName();
    }

    public boolean isRequireCondition() {
        return this.condition.isRequireCondition();
    }

    public H2Condition getCondition() {
        return this.condition;
    }

    public void setCondition(H2Condition condition) {
        this.condition = condition;
    }
}
