package cn.oyzh.easyshell.tabs.local;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
import cn.oyzh.easyshell.trees.connect.ShellConnectTreeItem;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Cursor;

/**
 * 本地终端tab
 *
 * @author oyzh
 * @since 2025/04/24
 */
public class ShellLocalTab extends RichTab {

    public ShellLocalTab(ShellConnectTreeItem treeItem) {
        this.init(treeItem);
    }

    @Override
    protected String url() {
        return "/tabs/local/shellLocalTab.fxml";
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.localTerminal();
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
     * @param treeItem shell信息
     */
    public void init(ShellConnectTreeItem treeItem) {
        try {
            // 初始化shell连接
            this.controller().init(treeItem.value());
            // 刷新图标
            this.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public ShellLocalTabController controller() {
        return (ShellLocalTabController) super.controller();
    }

    /**
     * shell信息
     *
     * @return 当前shell信息
     */
    public ShellConnect shellConnect() {
        return this.controller().getShellConnect();
    }
}
