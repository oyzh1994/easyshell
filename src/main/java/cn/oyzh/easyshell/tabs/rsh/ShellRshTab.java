package cn.oyzh.easyshell.tabs.rsh;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
import cn.oyzh.easyshell.rsh.ShellRshClient;
import cn.oyzh.easyshell.tabs.ShellTermTab;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.Cursor;

/**
 * rsh tab
 *
 * @author oyzh
 * @since 2025-03-04
 */
public class ShellRshTab extends ShellTermTab {

    //public ShellRshTab(ShellConnect connect) {
    //    this.init(connect);
    //    ObjectWatcherManager.watch(this);
    //}

    @Override
    protected String url() {
        return "/tabs/rsh/shellRshTab.fxml";
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = ShellOsTypeComboBox.getGlyph(this.shellConnect().getOsType());
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
            super.init(connect);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected String getTabTitle() {
        return this.controller().shellConnect().getName() + "(" + this.shellConnect().getType().toUpperCase() + ")";
    }

    @Override
    public ShellRshTabController controller() {
        return (ShellRshTabController) super.controller();
    }

    @Override
    public ShellRshClient client() {
        return this.controller().getClient();
    }

    @Override
    public void runSnippet(String content) throws Exception {
        this.controller().runSnippet(content);
    }
    /**
     * 创建rsh标签页
     *
     * @param connect 连接
     * @return rsh标签页
     */
    public static ShellRshTab of(ShellConnect connect) {
        ShellRshTab tab = new ShellRshTab();
        tab.init(connect);
        return tab;
    }
}
