package cn.oyzh.easyshell.tabs.mosh;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
import cn.oyzh.easyshell.mosh.ShellMoshClient;
import cn.oyzh.easyshell.tabs.ShellTermTab;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.Cursor;

/**
 * mosh标签页
 *
 * @author oyzh
 * @since 2025-03-04
 */
public class ShellMoshTab extends ShellTermTab {

    //public ShellMoshTab(ShellConnect connect) {
    //    this.init(connect);
    //    ObjectWatcherManager.watch(this);
    //}

    @Override
    protected String url() {
        return "/tabs/mosh/shellMoshTab.fxml";
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

    @Override
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
        return this.shellConnect().getName() + "(" + this.shellConnect().getType().toUpperCase() + ")";
    }

    @Override
    public ShellMoshTabController controller() {
        return (ShellMoshTabController) super.controller();
    }

    @Override
    public ShellMoshClient client() {
        return this.controller().getClient();
    }

    @Override
    public void runSnippet(String content) throws Exception {
        this.controller().runSnippet(content);
    }
    /**
     * 创建实例
     *
     * @param connect 连接
     * @return 实例对象
     */
    public static ShellMoshTab of(ShellConnect connect) {
        ShellMoshTab tab = new ShellMoshTab();
        tab.init(connect);
        return tab;
    }
}
