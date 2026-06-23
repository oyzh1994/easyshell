package cn.oyzh.easyshell.tabs.mysql.terminal;

import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.tabs.mysql.ShellMysqlBaseTab;
import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.fx.gui.svg.glyph.TerminalSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.Cursor;

/**
 * mysql终端tab
 *
 * @author oyzh
 * @since 2026/06/16
 */
public class ShellMysqlTerminalTab extends ShellMysqlBaseTab {

    private ShellMysqlClient client;

    private String dbName;

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
        String title = "Terminal";
        if (this.client != null) {
            title = "Terminal@" + this.client.connectName();
        }
        this.setText(title);
    }

    public void init(ShellMysqlClient client, String dbName) {
        this.client = client;
        this.dbName = dbName;
        this.controller().init(client, dbName);
        this.flush();
    }

    @Override
    public ShellMysqlTerminalTabController controller() {
        return (ShellMysqlTerminalTabController) super.controller();
    }

    public ShellMysqlClient client() {
        return this.client;
    }

    public String dbName() {
        return this.dbName;
    }

    @Override
    public ShellMysqlDatabaseTreeItem dbItem() {
        return this.controller().dbItem();
    }
}
