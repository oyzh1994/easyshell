package cn.oyzh.easyshell.tabs.postgresql.terminal;

import cn.oyzh.easyshell.postgresql.ShellPostgresqlClient;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.tabs.postgresql.ShellPostgresqlBaseTab;
import cn.oyzh.easyshell.trees.postgresql.schema.ShellPostgresqlSchemaTreeItem;
import cn.oyzh.fx.gui.svg.glyph.TerminalSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import javafx.scene.Cursor;

/**
 * postgresql终端tab
 *
 * @author oyzh
 * @since 2026-06-29
 */
public class ShellPostgresqlTerminalTab extends ShellPostgresqlBaseTab {

    @Override
    public ShellPostgresqlTerminalTabController controller() {
        return (ShellPostgresqlTerminalTabController) super.controller();
    }

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "postgresql/terminal/shellPostgresqlTerminalTab.fxml";
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
        this.setText(title);
    }

    /**
     * 初始化
     *
     * @param dbItem db节点
     */
    public void init(ShellPostgresqlSchemaTreeItem dbItem) {
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
    public ShellPostgresqlClient client() {
        return this.controller().client();
    }

    @Override
    public ShellPostgresqlSchemaTreeItem dbItem() {
        return this.controller().getDbItem();
    }
}
