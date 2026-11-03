package cn.oyzh.easyshell.tabs.sqlite.terminal;

import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.easyshell.tabs.sqlite.ShellSqliteBaseTab;
import cn.oyzh.easyshell.trees.sqlite.root.ShellSqliteRootTreeItem;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.fx.gui.svg.glyph.TerminalSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.Cursor;

/**
 * SQLite 终端标签页
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteTerminalTab extends ShellSqliteBaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "sqlite/terminal/shellSqliteTerminalTab.fxml";
    }

    @Override
    public ShellSqliteTerminalTabController controller() {
        return (ShellSqliteTerminalTabController) super.controller();
    }

    /**
     * 初始化
     *
     * @param dbItem 数据库树节点
     */
    public void init(ShellSqliteRootTreeItem dbItem) {
        this.controller().init(dbItem);
        this.flush();
    }

    /**
     * 获取连接信息
     *
     * @return 连接信息
     */
    public ShellConnect shellConnect() {
        return this.controller().shellConnect();
    }

    @Override
    public ShellSqliteRootTreeItem dbItem() {
        return this.controller().getDbItem();
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
}
