package cn.oyzh.easyshell.tabs.rlogin;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
import cn.oyzh.easyshell.rlogin.ShellRLoginClient;
import cn.oyzh.easyshell.tabs.ShellTermTab;
import cn.oyzh.easyshell.trees.connect.ShellConnectTreeItem;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.Cursor;

/**
 * rlogin tab
 *
 * @author oyzh
 * @since 2025/05/27
 */
public class ShellRLoginTab extends ShellTermTab {

    public ShellRLoginTab(ShellConnect connect) {
        this.init(connect);
    }

    @Override
    protected String url() {
        return "/tabs/rlogin/shellRLoginTab.fxml";
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = ShellOsTypeComboBox.getGlyph(this.shellConnect().getOsType());
            graphic.setSizeStr("13");
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    /**
     * 初始化
     *
     * @param connect 连接
     */
    public void init(ShellConnect connect) {
        try {
            // 初始化shell连接
            this.controller().init(connect);
            // 刷新图标
            this.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected String getTabTitle() {
        return this.controller().shellConnect().getName() + "(" + this.shellConnect().getType().toUpperCase() + ")";
    }

    @Override
    public ShellRLoginTabController controller() {
        return (ShellRLoginTabController) super.controller();
    }

    @Override
    public ShellConnect shellConnect() {
        return this.controller().shellConnect();
    }

    /**
     * 获取shell客户端
     *
     * @return shell客户端
     */
    public ShellRLoginClient client() {
        return this.controller().getClient();
    }

    @Override
    public void runSnippet(String content) throws Exception {
        super.runSnippet(content);
        this.controller().runSnippet(content);
    }
}
