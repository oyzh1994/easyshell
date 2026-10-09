package cn.oyzh.easyshell.tabs.mariadb.terminal;

import cn.oyzh.easyshell.mariadb.ShellMariadbClient;
import cn.oyzh.easyshell.tabs.mariadb.ShellMariadbBaseTab;
import cn.oyzh.easyshell.trees.mariadb.database.ShellMariadbDatabaseTreeItem;
import cn.oyzh.fx.gui.svg.glyph.TerminalSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.Cursor;

/**
 * MariaDB 终端标签页
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbTerminalTab extends ShellMariadbBaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "mariadb/terminal/shellMariadbTerminalTab.fxml";
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
    public void init(ShellMariadbDatabaseTreeItem dbItem) {
        try {
            this.controller().init(dbItem);
            this.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public ShellMariadbTerminalTabController controller() {
        return (ShellMariadbTerminalTabController) super.controller();
    }

    /**
     * 获取客户端
     *
     * @return 客户端
     */
    public ShellMariadbClient client() {
        return this.controller().client();
    }

    @Override
    public ShellMariadbDatabaseTreeItem dbItem() {
        return this.controller().getDbItem();
    }
}
