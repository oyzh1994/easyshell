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
 * db查询tab
 *
 * @author oyzh
 * @since 2024/02/18
 */
public class ShellDamengProcedureDesignTab extends ShellDamengBaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "procedure/damengProcedureDesignTab.fxml";
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

    public DamengProcedure procedure() {
        return this.controller().getProcedure();
    }

    public String procedureName() {
        return this.procedure().getName();
    }

    @Override
    public ShellDamengSchemaTreeItem dbItem() {
        return this.controller().getDbItem();
    }

    public String schema() {
        return this.dbItem().schema();
    }

    public String connectName() {
        return this.dbItem().connectName();
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
