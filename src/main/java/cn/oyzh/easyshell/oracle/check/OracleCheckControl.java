package cn.oyzh.easyshell.oracle.check;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.i18n.I18nHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Oracle数据库检查约束编辑控件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class OracleCheckControl extends OracleCheck {

    /**
     * 创建名称编辑控件
     *
     * @return 名称编辑控件
     */
    public ClearableTextField getNameControl() {
        ClearableTextField textField = new ClearableTextField();
        textField.setPromptText(I18nHelper.pleaseInputName());
        if (StringUtil.isEmpty(this.getName())) {
            this.setName(DBUtil.genCheckName());
        }
        textField.addTextChangeListener((observable, oldValue, newValue) -> this.setName(newValue));
        textField.setText(this.getName());
        TableViewUtil.rowOnCtrlS(textField);
        TableViewUtil.selectRowOnMouseClicked(textField);
        return textField;
    }

    /**
     * 创建子语句编辑控件
     *
     * @return 子语句编辑控件
     */
    public ClearableTextField getClauseControl() {
        ClearableTextField textField = new ClearableTextField();
        textField.setPromptText(I18nHelper.pleaseInputName());
        textField.addTextChangeListener((observable, oldValue, newValue) -> this.setClause(newValue));
        textField.setText(this.getClause());
        TableViewUtil.rowOnCtrlS(textField);
        TableViewUtil.selectRowOnMouseClicked(textField);
        return textField;
    }

    /**
     * 将检查约束转换为检查约束编辑控件
     *
     * @param check 检查约束
     * @return 检查约束编辑控件
     */
    public static OracleCheckControl of(OracleCheck check) {
        OracleCheckControl control = new OracleCheckControl();
        control.copy(check);
        return control;
    }

    /**
     * 将检查约束列表转换为检查约束编辑控件列表
     *
     * @param checks 检查约束列表
     * @return 检查约束编辑控件列表
     */
    public static List<OracleCheckControl> of(List<OracleCheck> checks) {
        List<OracleCheckControl> controls = new ArrayList<>();
        for (OracleCheck check : checks) {
            controls.add(of(check));
        }
        return controls;
    }
}
