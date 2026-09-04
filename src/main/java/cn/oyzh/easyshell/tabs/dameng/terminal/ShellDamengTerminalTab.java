package cn.oyzh.easyshell.tabs.dameng.terminal;

import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.tabs.dameng.ShellDamengBaseTab;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.fx.gui.svg.glyph.TerminalSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import javafx.scene.Cursor;

/**
 * dameng终端tab
 *
 * @author oyzh
 * @since 2023/7/21
 */
public class ShellDamengTerminalTab extends ShellDamengBaseTab {

    @Override
    public ShellDamengTerminalTabController controller() {
        return (ShellDamengTerminalTabController) super.controller();
    }

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "dameng/terminal/shellDamengTerminalTab.fxml";
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
    public void init(ShellDamengSchemaTreeItem dbItem) {
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

    public ShellDamengClient client() {
        return this.controller().client();
    }

    @Override
    public ShellDamengSchemaTreeItem dbItem() {
        return this.controller().getDbItem();
    }
}
