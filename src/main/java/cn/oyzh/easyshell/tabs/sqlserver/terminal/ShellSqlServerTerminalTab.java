package cn.oyzh.easyshell.tabs.sqlserver.terminal;

import cn.oyzh.easyshell.sqlserver.ShellSqlServerClient;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.tabs.sqlserver.ShellSqlServerBaseTab;
import cn.oyzh.easyshell.trees.sqlserver.schema.ShellSqlServerSchemaTreeItem;
import cn.oyzh.fx.gui.svg.glyph.TerminalSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import javafx.scene.Cursor;

/**
 * sqlserver终端tab
 *
 * @author oyzh
 * @since 2026-06-29
 */
public class ShellSqlServerTerminalTab extends ShellSqlServerBaseTab {

    @Override
    public ShellSqlServerTerminalTabController controller() {
        return (ShellSqlServerTerminalTabController) super.controller();
    }

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "sqlserver/terminal/shellSqlServerTerminalTab.fxml";
    }

    @Override
    public void flushGraphic() {
        TerminalSVGGlyph graphic = (TerminalSVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new TerminalSVGGlyph();
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    public void flushTitle() {
        String title = this.schema();
        if (this.client() != null) {
            title = title + "(" + this.client().connectName() + ")";
        }
        this.text(title);
    }

    /**
     * 初始化
     *
     * @param dbItem db节点
     */
    public void init(ShellSqlServerSchemaTreeItem dbItem) {
        this.controller().init(dbItem);
        this.flush();
    }

    /**
     * db信息
     *
     * @return 当前db信息
     */
    public ShellConnect shellConnect() {
        return this.controller().shellConnect();
    }

    /**
     * 获取客户端
     *
     * @return 客户端
     */
    public ShellSqlServerClient client() {
        return this.controller().client();
    }

    @Override
    public ShellSqlServerSchemaTreeItem dbItem() {
        return this.controller().getDbItem();
    }
}
