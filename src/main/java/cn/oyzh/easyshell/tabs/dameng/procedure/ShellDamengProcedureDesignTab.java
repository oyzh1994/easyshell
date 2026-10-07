package cn.oyzh.easyshell.tabs.dameng.procedure;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.procedure.DamengProcedure;
import cn.oyzh.easyshell.tabs.dameng.ShellDamengBaseTab;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.fx.gui.svg.glyph.database.ProcedureSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * 达梦存储过程设计标签页，负责过程信息的展示与设计维护
 *
 * @author oyzh
 * @since 2024/02/18
 */
public class ShellDamengProcedureDesignTab extends ShellDamengBaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "dameng/procedure/shellDamengProcedureDesignTab.fxml";
    }

    @Override
    public void flushGraphic() {
        ProcedureSVGGlyph graphic = (ProcedureSVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new ProcedureSVGGlyph();
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    public void flushTitle() {
        String name = this.procedureName();
        if (StringUtil.isBlank(name)) {
            name = I18nHelper.unnamedProcedure();
        }
        // 设置提示文本
        if (this.isUnsaved()) {
            this.setText("* " + name + "@" + this.schema() + "(" + this.connectName() + ")");
        } else {
            this.setText(name + "@" + this.schema() + "(" + this.connectName() + ")");
        }
    }

    /**
     * 获取过程对象
     *
     * @return 过程对象
     */
    public DamengProcedure procedure() {
        return this.controller().getProcedure();
    }

    /**
     * 获取过程名称
     *
     * @return 过程名称
     */
    public String procedureName() {
        return this.procedure().getName();
    }

    @Override
    public ShellDamengSchemaTreeItem dbItem() {
        return this.controller().getDbItem();
    }

    /**
     * 初始化
     *
     * @param procedure 查询对象
     * @param item      db库树节点
     */
    public void init(DamengProcedure procedure, ShellDamengSchemaTreeItem item) {
        this.controller().init(procedure, item);
        // 刷新tab
        this.flush();
    }

    @Override
    public ShellDamengProcedureDesignTabController controller() {
        return (ShellDamengProcedureDesignTabController) super.controller();
    }

    /**
     * 是否未保存
     *
     * @return 是否未保存
     */
    public boolean isUnsaved() {
        return this.controller().isUnsaved();
    }

    @Override
    protected void onTabCloseRequest(Event event) {
        if (this.isUnsaved() && !MessageBox.confirm(I18nHelper.unsavedAndContinue())) {
            event.consume();
        } else {
            this.closeTab();
        }
    }
}
