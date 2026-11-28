package cn.oyzh.easyshell.tabs.h2.terminal;

import cn.oyzh.easyshell.h2.ShellH2Client;
import cn.oyzh.easyshell.tabs.h2.ShellH2BaseTab;
import cn.oyzh.easyshell.trees.h2.root.ShellH2RootTreeItem;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.fx.gui.svg.glyph.TerminalSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.Cursor;

/**
 * H2 终端标签页
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellH2TerminalTab extends ShellH2BaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "h2/terminal/shellH2TerminalTab.fxml";
    }

    @Override
    public ShellH2TerminalTabController controller() {
        return (ShellH2TerminalTabController) super.controller();
    }

    /**
     * 初始化
     *
     * @param dbItem 数据库树节点
     */
    public void init(ShellH2RootTreeItem dbItem) {
        this.controller().init(dbItem);
        this.flush();
    }

    /**
     * 初始化指定模式终端。
     *
     * @param dbItem 数据库树节点
     * @param schema 模式名称
     */
    public void init(ShellH2RootTreeItem dbItem, String schema) {
        this.controller().init(dbItem, schema);
        this.flush();
    }

    @Override
    public String dbName() {
        return this.controller().getDbName();
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
    public ShellH2RootTreeItem dbItem() {
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
        this.text(title);
    }
}
