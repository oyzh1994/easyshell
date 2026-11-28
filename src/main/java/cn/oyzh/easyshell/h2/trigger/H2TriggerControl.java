package cn.oyzh.easyshell.h2.trigger;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.fx.h2.table.ShellH2TriggerPolicyComboBox;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.editor.incubator.EditorFormatType;
import cn.oyzh.fx.editor.incubator.control.EditorEnlargeTextFiled;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.i18n.I18nHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * H2 触发器设计组件。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2TriggerControl extends H2Trigger {

    public ClearableTextField getNameControl() {
        ClearableTextField field = new ClearableTextField();
        if (StringUtil.isEmpty(this.getName())) {
            this.setName(DBUtil.genTriggerName());
        }
        field.setPromptText(I18nHelper.pleaseInputName());
        field.setText(this.getName());
        field.addTextChangeListener((observable, oldValue, newValue) -> {
            this.setName(newValue);
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
    public ShellH2TriggerPolicyComboBox getPolicyControl() {
        ShellH2TriggerPolicyComboBox comboBox = new ShellH2TriggerPolicyComboBox();
        comboBox.selectedItemChanged((observable, oldValue, newValue) -> {
            this.setPolicy(newValue);
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
        });
        TableViewUtil.rowOnCtrlS(field);
        TableViewUtil.selectRowOnMouseClicked(field);
        return field;
    }

    public static H2TriggerControl of(H2Trigger trigger) {
        H2TriggerControl control = new H2TriggerControl();
        control.copy(trigger);
        return control;
    }

    public static List<H2TriggerControl> of(List<H2Trigger> triggers) {
        List<H2TriggerControl> controls = new ArrayList<>();
        if (triggers != null) {
            for (H2Trigger trigger : triggers) {
                controls.add(of(trigger));
            }
        }
        return controls;
    }
}
