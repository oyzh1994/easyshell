package cn.oyzh.easyshell.mysql.check;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.i18n.I18nHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * MySQL检查约束组件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class MysqlCheckControl extends MysqlCheck {

    /**
     * 获取名称组件
     *
     * @return 名称组件
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
     * 获取子语句组件
     *
     * @return 子语句组件
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
     * 根据检查约束构建组件
     *
     * @param check 检查约束
     * @return 组件
     */
    public static MysqlCheckControl of(MysqlCheck check) {
        MysqlCheckControl control = new MysqlCheckControl();
        control.copy(check);
        return control;
    }

    /**
     * 根据检查约束列表构建组件列表
     *
     * @param checks 检查约束列表
     * @return 组件列表
     */
    public static List<MysqlCheckControl> of(List<MysqlCheck> checks) {
        List<MysqlCheckControl> controls = new ArrayList<>();
        for (MysqlCheck check : checks) {
            controls.add(of(check));
        }
        return controls;
    }
}
