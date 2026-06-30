package cn.oyzh.easyshell.tabs.mongo.terminal;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.fx.gui.svg.glyph.TerminalSVGGlyph;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Cursor;

/**
 * mongodb终端tab
 *
 * @author oyzh
 * @since 2023/7/21
 */
public class ShellMongoTerminalTab extends RichTab {

    public ShellMongoTerminalTab(ShellMongoClient client, String dbName) {
        this.init(client, dbName);
    }

    @Override
    public ShellMongoTerminalTabController controller() {
        return (ShellMongoTerminalTabController) super.controller();
    }

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "mongo/terminal/shellMongoTerminalTab.fxml";
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
    protected String getTabTitle() {
        ShellConnect shellConnect = this.shellConnect();
        return shellConnect.getName();
    }

    /**
     * 初始化
     *
     * @param client mongodb客户端
     */
    private void init(ShellMongoClient client, String dbName) {
        try {
            if (client == null) {
                ShellConnect connect = new ShellConnect();
                connect.setName(I18nHelper.unnamedConnection());
                // 刷新图标
                this.flushGraphic();
                // 初始化mongodb连接
                this.controller().init(new ShellMongoClient(connect), dbName);
            } else {
                // 刷新图标
                this.flushGraphic();
                // 初始化mongodb连接
                this.controller().init(client, dbName);
            }
            this.flushTitle();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * mongodb信息
     *
     * @return 当前mongodb信息
     */
    public ShellConnect shellConnect() {
        return this.controller().shellConnect();
    }

    public ShellMongoClient client() {
        return this.controller().client();
    }

    public String dbName() {
        return this.controller().getDbName();
    }
}
