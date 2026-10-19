package cn.oyzh.easyshell.tabs.mariadb.procedure;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mariadb.procedure.MariadbProcedure;
import cn.oyzh.easyshell.tabs.mariadb.ShellMariadbBaseTab;
import cn.oyzh.easyshell.trees.mariadb.database.ShellMariadbDatabaseTreeItem;
import cn.oyzh.fx.gui.svg.glyph.database.ProcedureSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * MariaDB 存储过程设计标签页
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbProcedureDesignTab extends ShellMariadbBaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "mariadb/procedure/shellMariadbProcedureDesignTab.fxml";
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
            this.setText("* " + name + "@" + this.dbName() + "(" + this.connectName() + ")");
        } else {
            this.setText(name + "@" + this.dbName() + "(" + this.connectName() + ")");
        }
    }

    /**
     * 获取过程对象
     *
     * @return 过程对象
     */
    public MariadbProcedure procedure() {
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
    public ShellMariadbDatabaseTreeItem dbItem() {
        return this.controller().getDbItem();
    }

    /**
     * 初始化
     *
     * @param procedure 过程对象
     * @param item      数据库树节点
     */
    public void init(MariadbProcedure procedure, ShellMariadbDatabaseTreeItem item) {
        this.controller().init(procedure, item);
        // 刷新tab
        this.flush();
    }

    @Override
    public ShellMariadbProcedureDesignTabController controller() {
        return (ShellMariadbProcedureDesignTabController) super.controller();
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
