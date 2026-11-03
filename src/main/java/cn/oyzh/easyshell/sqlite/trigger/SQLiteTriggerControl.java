package cn.oyzh.easyshell.sqlite.trigger;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.fx.sqlite.table.ShellSqliteTriggerPolicyComboBox;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.editor.incubator.EditorFormatType;
import cn.oyzh.fx.editor.incubator.control.EditorEnlargeTextFiled;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.i18n.I18nHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite 触发器设计组件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteTriggerControl extends SQLiteTrigger {

    public ClearableTextField getNameControl() {
        ClearableTextField field = new ClearableTextField();
        if (StringUtil.isEmpty(this.getName())) {
            this.setName(DBUtil.genTriggerName());
        }
        field.setPromptText(I18nHelper.pleaseInputName());
        field.setText(this.getName());
        field.addTextChangeListener((observable, oldValue, newValue) -> {
            this.setName(newValue);
            this.setChanged(true);
        });
        TableViewUtil.rowOnCtrlS(field);
        TableViewUtil.selectRowOnMouseClicked(field);
        return field;
    }

    /**
     * 获取策略组件。
     *
     * @return 策略组件
     */
    public ShellSqliteTriggerPolicyComboBox getPolicyControl() {
        ShellSqliteTriggerPolicyComboBox comboBox = new ShellSqliteTriggerPolicyComboBox();
        comboBox.selectedItemChanged((observable, oldValue, newValue) -> {
            this.setPolicy(newValue);
            this.setChanged(true);
        });
        comboBox.selectFirstIfNull(this.getPolicy());
        TableViewUtil.rowOnCtrlS(comboBox);
        TableViewUtil.selectRowOnMouseClicked(comboBox);
        return comboBox;
    }

    public EditorEnlargeTextFiled getDefinitionControl() {
        EditorEnlargeTextFiled field = new EditorEnlargeTextFiled();
        field.setFormatType(EditorFormatType.SQL);
        field.setPromptText(I18nHelper.pleaseInputContent());
        field.setText(this.getDefinition());
        field.addTextChangeListener((observable, oldValue, newValue) -> {
            this.setDefinition(newValue);
            this.setChanged(true);
        });
        TableViewUtil.rowOnCtrlS(field);
        TableViewUtil.selectRowOnMouseClicked(field);
        return field;
    }

    public static SQLiteTriggerControl of(SQLiteTrigger trigger) {
        SQLiteTriggerControl control = new SQLiteTriggerControl();
        control.copy(trigger);
        return control;
    }

    public static List<SQLiteTriggerControl> of(List<SQLiteTrigger> triggers) {
        List<SQLiteTriggerControl> controls = new ArrayList<>();
        if (triggers != null) {
            for (SQLiteTrigger trigger : triggers) {
                controls.add(of(trigger));
            }
        }
        return controls;
    }
}
