package cn.oyzh.easyshell.tabs.zk.terminal;

import cn.oyzh.easyshell.domain.zk.ZKConnect;
import cn.oyzh.easyshell.zk.ZKClient;
import cn.oyzh.fx.gui.svg.glyph.TerminalSVGGlyph;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Cursor;

/**
 * zk终端tab
 *
 * @author oyzh
 * @since 2023/7/21
 */
public class ZKTerminalTab extends RichTab {

    public ZKTerminalTab(ZKClient client) {
        this.init(client);
    }
//
//    public ZKTerminalTab(ZKConnect connect) {
//        this.init(connect);
//    }

    @Override
    public void flushGraphic() {
        TerminalSVGGlyph glyph = (TerminalSVGGlyph) this.getGraphic();
        if (glyph == null) {
            glyph = new TerminalSVGGlyph("12");
            glyph.setCursor(Cursor.DEFAULT);
            this.graphic(glyph);
        }
    }

//    /**
//     * 初始化
//     *
//     * @param zkConnect zk连接
//     */
//    public void init(ZKConnect zkConnect) {
//        try {
//            if (zkConnect == null) {
//                zkConnect = new ZKConnect();
//                zkConnect.setName(I18nHelper.unnamedConnection());
//            }
//            // 刷新图标
//            this.flushGraphic();
//            // 设置标题
//            super.setTitle(zkConnect.getName());
//            // 初始化zk连接
//            this.controller().client(new ZKClient(zkConnect));
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }

    @Override
    protected String getTabTitle() {
        return this.zkConnect().getName();
    }

    /**
     * 初始化
     *
     * @param client zk客户端
     */
    private void init(ZKClient client) {
        try {
            if (client == null) {
                ZKConnect connect = new ZKConnect();
                connect.setName(I18nHelper.unnamedConnection());
                // 刷新图标
                this.flushGraphic();
//                // 设置标题
//                super.setTitle(connect.getName());
                // 初始化zk连接
                this.controller().client(new ZKClient(connect));
            } else {
                // 刷新图标
                this.flushGraphic();
//                // 设置标题
//                super.setTitle(client.zkConnect().getName());
                // 初始化zk连接
                this.controller().client(client);
            }
            this.flushTitle();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 获取zk客户端
     *
     * @return zk客户端
     */
    public ZKClient client() {
        return this.controller().client();
    }

    /**
     * 获取zk信息
     *
     * @return zk信息
     */
    public ZKConnect zkConnect() {
        return this.controller().zkConnect();
    }

    @Override
    public ZKTerminalTabController controller() {
        return (ZKTerminalTabController) super.controller();
    }

    @Override
    protected String url() {
        return "/tabs/terminal/zkTerminalTab.fxml";
    }

}
