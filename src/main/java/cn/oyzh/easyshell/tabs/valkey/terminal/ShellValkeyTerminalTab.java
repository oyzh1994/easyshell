package cn.oyzh.easyshell.tabs.valkey.terminal;

import cn.oyzh.easyshell.tabs.valkey.ShellValkeyBaseTab;
import cn.oyzh.easyshell.trees.valkey.database.ShellValkeyDatabaseTreeItem;
import cn.oyzh.fx.gui.svg.glyph.TerminalSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Cursor;

/**
 * redis终端标签页
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellValkeyTerminalTab extends ShellValkeyBaseTab {

    /**
     * 数据库节点
     */
    private ShellValkeyDatabaseTreeItem dbItem;

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "valkey/terminal/shellValkeyTerminalTab.fxml";
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
        this.setText(I18nHelper.terminal() + "@" + this.dbName() + "(" + this.connectName() + ")");
    }

    /**
     * 初始化
     *
     * @param dbItem 数据库节点
     */
    public void init(ShellValkeyDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
        this.controller().init(dbItem);
        this.flush();
    }

    @Override
    public ShellValkeyTerminalTabController controller() {
        return (ShellValkeyTerminalTabController) super.controller();
    }

    @Override
    public ShellValkeyDatabaseTreeItem dbItem() {
        return dbItem;
    }
}
