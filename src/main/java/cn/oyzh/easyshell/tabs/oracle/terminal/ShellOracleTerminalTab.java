package cn.oyzh.easyshell.tabs.oracle.terminal;

import cn.oyzh.easyshell.oracle.ShellOracleClient;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.tabs.oracle.ShellOracleBaseTab;
import cn.oyzh.easyshell.trees.oracle.schema.ShellOracleSchemaTreeItem;
import cn.oyzh.fx.gui.svg.glyph.TerminalSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import javafx.scene.Cursor;

/**
 * oracle终端tab
 *
 * @author oyzh
 * @since 2026-06-29
 */
public class ShellOracleTerminalTab extends ShellOracleBaseTab {

    @Override
    public ShellOracleTerminalTabController controller() {
        return (ShellOracleTerminalTabController) super.controller();
    }

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "oracle/terminal/shellOracleTerminalTab.fxml";
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
    public void init(ShellOracleSchemaTreeItem dbItem) {
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
    public ShellOracleClient client() {
        return this.controller().client();
    }

    @Override
    public ShellOracleSchemaTreeItem dbItem() {
        return this.controller().getDbItem();
    }
}
