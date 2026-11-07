package cn.oyzh.easyshell.tabs.zk.node;

import cn.oyzh.easyshell.fx.svg.glyph.zk.NodeSVGGlyph;
import cn.oyzh.easyshell.trees.zk.node.ShellZKNodeTreeItem;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * zk节点标签页
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellZKNodeTab extends RichTab {

    @Override
    protected String url() {
        return "/tabs/zk/node/shellZKNodeTab.fxml";
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new NodeSVGGlyph();
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    public void flushTitle() {
        ShellZKNodeTreeItem item = this.item();
        if (item != null) {
            this.setText(item.decodeNodePath() + "(" + item.connectName() + ")");
        }
    }

    /**
     * 初始化
     *
     * @param item 节点
     */
    public void init(ShellZKNodeTreeItem item) {
        this.controller().init(item);
        this.flush();
    }

    @Override
    public ShellZKNodeTabController controller() {
        return (ShellZKNodeTabController) super.controller();
    }

    /**
     * 获取节点
     *
     * @return 节点
     */
    public ShellZKNodeTreeItem item() {
        ShellZKNodeTabController controller = this.controller();
        return controller == null ? null : controller.getActiveItem();
    }

    /**
     * 获取节点路径
     *
     * @return 节点路径
     */
    public String nodePath() {
        ShellZKNodeTreeItem item = this.item();
        return item == null ? null : item.nodePath();
    }

    /**
     * 是否有未保存数据
     *
     * @return 结果
     */
    public boolean hasUnsavedData() {
        ShellZKNodeTabController controller = this.controller();
        return controller != null && controller.hasUnsavedData();
    }

    @Override
    protected void onTabCloseRequest(Event event) {
        if (this.hasUnsavedData() && !MessageBox.confirm(I18nHelper.unsavedAndContinue(), this.getWindow())) {
            event.consume();
        } else {
            this.closeTab();
        }
    }
}
