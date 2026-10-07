package cn.oyzh.easyshell.tabs.mysql.terminal;

import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.tabs.mysql.ShellMysqlBaseTab;
import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.fx.gui.svg.glyph.TerminalSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.Cursor;

/**
 * MySQL 终端标签页
 *
 * @author oyzh
 * @since 2026/06/16
 */
public class ShellMysqlTerminalTab extends ShellMysqlBaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "mysql/terminal/shellMysqlTerminalTab.fxml";
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new TerminalSVGGlyph();
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    public void flushTitle() {
        String title = this.dbName();
        if (this.client() != null) {
            title = title + "(" + this.client().connectName() + ")";
        }
        this.setText(title);
    }

    /**
     * 初始化
     *
     * @param dbItem 数据库树节点
     */
    public void init(ShellMysqlDatabaseTreeItem dbItem) {
        try {
            this.controller().init(dbItem);
            this.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public ShellMysqlTerminalTabController controller() {
        return (ShellMysqlTerminalTabController) super.controller();
    }

    /**
     * 获取客户端
     *
     * @return 客户端
     */
    public ShellMysqlClient client() {
        return this.controller().client();
    }

    @Override
    public ShellMysqlDatabaseTreeItem dbItem() {
        return this.controller().getDbItem();
    }
}
