package cn.oyzh.easyshell.postgresql.trigger;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.fx.postgresql.table.ShellPostgresqlTriggerPolicyComboBox;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.editor.incubator.EditorFormatType;
import cn.oyzh.fx.editor.incubator.control.EditorEnlargeTextFiled;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.i18n.I18nHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * PostgreSQL触发器组件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class PostgresqlTriggerControl extends PostgresqlTrigger {

    /**
     * 获取名称组件
     *
     * @return 名称组件
     */
    public ClearableTextField getNameControl() {
        ClearableTextField textField = new ClearableTextField();
        textField.setPromptText(I18nHelper.pleaseInputName());
        if (StringUtil.isEmpty(this.getName())) {
            this.setName(DBUtil.genTriggerName());
        }
        textField.addTextChangeListener((observable, oldValue, newValue) -> {
            this.setName(newValue);
        });
        textField.setText(this.getName());
        TableViewUtil.rowOnCtrlS(textField);
        TableViewUtil.selectRowOnMouseClicked(textField);
        return textField;
    }

    /**
     * 获取策略组件
     *
     * @return 策略组件
     */
    public ShellPostgresqlTriggerPolicyComboBox getPolicyControl() {
        ShellPostgresqlTriggerPolicyComboBox comboBox = new ShellPostgresqlTriggerPolicyComboBox();
        comboBox.selectedItemChanged((observable, oldValue, newValue) -> {
            this.setPolicy(newValue);
        });
        comboBox.selectFirstIfNull(this.getPolicy());
        TableViewUtil.rowOnCtrlS(comboBox);
        TableViewUtil.selectRowOnMouseClicked(comboBox);
        return comboBox;
    }

    /**
     * 获取定义组件
     *
     * @return 定义组件
     */
    public EditorEnlargeTextFiled getDefinitionControl() {
        EditorEnlargeTextFiled textField = new EditorEnlargeTextFiled();
        textField.setFormatType(EditorFormatType.SQL);
        textField.setPromptText(I18nHelper.pleaseInputContent());
        textField.addTextChangeListener((observable, oldValue, newValue) -> this.setDefinition(newValue));
        textField.setText(this.getDefinition());
        TableViewUtil.rowOnCtrlS(textField);
        TableViewUtil.selectRowOnMouseClicked(textField);
        return textField;
    }

    /**
     * 根据触发器构建组件
     *
     * @param trigger 触发器
     * @return 组件
     */
    public static PostgresqlTriggerControl of(PostgresqlTrigger trigger) {
        PostgresqlTriggerControl control = new PostgresqlTriggerControl();
        control.copy(trigger);
        return control;
    }

    /**
     * 根据触发器列表构建组件列表
     *
     * @param triggers 触发器列表
     * @return 组件列表
     */
    public static List<PostgresqlTriggerControl> of(List<PostgresqlTrigger> triggers) {
        List<PostgresqlTriggerControl> controls = new ArrayList<>();
        for (PostgresqlTrigger trigger : triggers) {
            controls.add(of(trigger));
        }
        return controls;
    }
}
